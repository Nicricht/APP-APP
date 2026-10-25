package cl.duocuc.segurosbpo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.content.Context
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.ValidadorBorradorEvidencia
import kotlinx.coroutines.launch

private fun nombreArchivoSeleccionado(context: Context, uri: Uri): String = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) it.getString(0) else null
    }
}.getOrNull()?.takeIf { it.isNotBlank() } ?: "Archivo seleccionado"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenciasScreen(
    siniestro: Siniestro?,
    asegurado: Asegurado,
    evidencias: List<Evidencia>,
    solicitudes: List<SolicitudDocumento> = emptyList(),
    onVolver: () -> Unit,
    onCargar: () -> Unit,
    onCargarImagen: suspend (Long) -> Bitmap?,
    onGuardarFoto: suspend (String, Bitmap) -> String?,
    onGuardarArchivo: suspend (String, Uri) -> String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aviso = remember { SnackbarHostState() }
    var tipoDocumento by rememberSaveable(siniestro?.id) { mutableStateOf("") }
    var mostrarError by rememberSaveable(siniestro?.id) { mutableStateOf(false) }
    var imagenVista by remember(siniestro?.id) { mutableStateOf<Bitmap?>(null) }
    var identificadorImagen by remember(siniestro?.id) { mutableStateOf<Long?>(null) }
    var fotoPendiente by remember(siniestro?.id) { mutableStateOf<Bitmap?>(null) }
    var archivoPendiente by remember(siniestro?.id) { mutableStateOf<Uri?>(null) }
    var origenPendiente by remember(siniestro?.id) { mutableStateOf("") }
    var guardando by remember(siniestro?.id) { mutableStateOf(false) }

    LaunchedEffect(siniestro?.id, asegurado) { onCargar() }

    fun validarTipo(accion: () -> Unit) {
        mostrarError = tipoDocumento.isBlank()
        if (!mostrarError) accion()
    }

    fun avisarResultado(error: String?) {
        scope.launch {
            aviso.showSnackbar(error ?: "Documento guardado en el expediente local")
        }
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) {
            fotoPendiente = foto
            archivoPendiente = null
            origenPendiente = "Fotografía tomada"
        } else {
            scope.launch { aviso.showSnackbar("Captura cancelada") }
        }
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) camara.launch(null)
        else scope.launch { aviso.showSnackbar("Permiso de cámara denegado") }
    }
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            archivoPendiente = uri
            fotoPendiente = null
            origenPendiente = nombreArchivoSeleccionado(context, uri)
        }
    }
    val documentos = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            archivoPendiente = uri
            fotoPendiente = null
            origenPendiente = nombreArchivoSeleccionado(context, uri)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Documentos y evidencias") }) },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver al siniestro") }
            if (siniestro == null) {
                Text("Este siniestro no pertenece al asegurado seleccionado")
            } else {
                Text(siniestro.numero, style = MaterialTheme.typography.titleLarge)
                Text("Evidencias guardadas: ${evidencias.size}/15")
                Text("Documentos solicitados", style = MaterialTheme.typography.titleMedium)
                if (solicitudes.isEmpty()) Text("No hay solicitudes de documentos por ahora.")
                solicitudes.forEach { solicitud ->
                    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(solicitud.tipo, style = MaterialTheme.typography.titleSmall)
                            Text(if (solicitud.pendiente) "Pendiente de entrega" else "Documento registrado")
                            Text("Solicitado: " + solicitud.fechaSolicitud, style = MaterialTheme.typography.bodySmall)
                            if (solicitud.comentario.isNotBlank()) Text(solicitud.comentario)
                            if (solicitud.pendiente && siniestro.etapa != EtapaSiniestro.CERRADO) {
                                TextButton(onClick = { tipoDocumento = solicitud.tipo }) {
                                    Text("Preparar entrega de este documento")
                                }
                            }
                        }
                    }
                }
                Text("Expediente local: los archivos se conservan en este dispositivo.",
                    style = MaterialTheme.typography.bodySmall)
                if (siniestro.etapa == EtapaSiniestro.CERRADO) {
                    Text("No se pueden agregar evidencias a un siniestro cerrado.")
                } else {
                    OutlinedTextField(
                        value = tipoDocumento,
                        onValueChange = { tipoDocumento = it; mostrarError = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tipo de documento *") },
                        placeholder = { Text("Ej.: fotografía del daño") },
                        isError = mostrarError,
                        supportingText = {
                            Text(if (mostrarError) "Indica el tipo de documento" else "Obligatorio")
                        },
                        singleLine = true
                    )
                    Button(
                        enabled = evidencias.size < 15,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            validarTipo {
                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) camara.launch(null)
                                else permisoCamara.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) { Text("Tomar fotografía") }
                    OutlinedButton(
                        enabled = evidencias.size < 15,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { validarTipo { galeria.launch("image/*") } }
                    ) { Text("Elegir de la galería") }
                    OutlinedButton(
                        enabled = evidencias.size < 15,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { validarTipo { documentos.launch("application/pdf") } }
                    ) { Text("Adjuntar PDF") }
                    Text("Se permiten JPG, PNG, WEBP o PDF. Máximo 5 MB por archivo.",
                        style = MaterialTheme.typography.bodySmall)
                    if (fotoPendiente != null || archivoPendiente != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Documento listo para guardar",
                                    style = MaterialTheme.typography.titleMedium)
                                Text(origenPendiente)
                                fotoPendiente?.let { foto ->
                                    Image(
                                        bitmap = foto.asImageBitmap(),
                                        contentDescription = "Vista previa de la fotografía",
                                        modifier = Modifier.fillMaxWidth().height(180.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text("Comprueba el tipo de documento antes de guardarlo.")
                                Button(
                                    enabled = !guardando && ValidadorBorradorEvidencia.puedeGuardar(
                                        tipoDocumento, true, evidencias.size
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        guardando = true
                                        scope.launch {
                                            val error = try {
                                                val tipo = tipoDocumento.trim()
                                                val foto = fotoPendiente
                                                val archivo = archivoPendiente
                                                when {
                                                    foto != null -> onGuardarFoto(tipo, foto)
                                                    archivo != null -> onGuardarArchivo(tipo, archivo)
                                                    else -> "Selecciona un documento"
                                                }
                                            } catch (e: Exception) {
                                                e.message ?: "No se pudo guardar el documento"
                                            } finally {
                                                guardando = false
                                            }
                                            if (error == null) {
                                                fotoPendiente = null
                                                archivoPendiente = null
                                                origenPendiente = ""
                                                tipoDocumento = ""
                                                onCargar()
                                            }
                                            avisarResultado(error)
                                        }
                                    }
                                ) { Text(if (guardando) "Guardando…" else "Guardar documento") }
                                TextButton(onClick = {
                                    fotoPendiente = null
                                    archivoPendiente = null
                                    origenPendiente = ""
                                }) { Text("Cancelar selección") }
                            }
                        }
                    }
                }
                Text("Documentos registrados", style = MaterialTheme.typography.titleMedium)
                if (evidencias.isEmpty()) {
                    Text("Aún no hay archivos para este siniestro.")
                }
                evidencias.forEach { evidencia ->
                    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(evidencia.tipoDocumento, style = MaterialTheme.typography.titleSmall)
                            Text(evidencia.nombreArchivo)
                            Text("Registrado: ${evidencia.fechaRegistro}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("${evidencia.tamanoBytes / 1024} KB · ${evidencia.tipoMime}",
                                style = MaterialTheme.typography.bodySmall)
                            if (evidencia.tipoMime.startsWith("image/")) {
                                TextButton(onClick = {
                                    scope.launch {
                                        imagenVista = onCargarImagen(evidencia.id)
                                        identificadorImagen = evidencia.id
                                        if (imagenVista == null) aviso.showSnackbar("No se pudo abrir la imagen")
                                    }
                                }) { Text("Ver imagen") }
                                if (identificadorImagen == evidencia.id && imagenVista != null) {
                                    Image(
                                        bitmap = imagenVista!!.asImageBitmap(),
                                        contentDescription = "Vista previa: ${evidencia.tipoDocumento}",
                                        modifier = Modifier.fillMaxWidth().height(220.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
