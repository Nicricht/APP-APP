package cl.duocuc.segurosbpo.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.DocumentosLiquidador
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.permisos.Permisos
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosLiquidadorScreen(
    siniestro: Siniestro?,
    evidencias: List<Evidencia>,
    onVolver: () -> Unit,
    onCargar: () -> Unit,
    onCargarImagen: suspend (Long) -> Bitmap?,
    onCargarMiniatura: suspend (Long) -> Bitmap?,
    onDescargar: suspend (Long, Uri) -> String?,
    onGuardarFoto: suspend (String, Bitmap) -> String?,
    onGuardarArchivo: suspend (String, Uri) -> String?,
    onEliminar: (Long) -> String?
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    val aviso = remember { SnackbarHostState() }
    var tipoElegido by rememberSaveable(siniestro?.id) { mutableStateOf(DocumentosLiquidador.tipos.first()) }
    var imagenVista by remember(siniestro?.id) { mutableStateOf<Bitmap?>(null) }
    var porEliminar by remember(siniestro?.id) { mutableStateOf<Evidencia?>(null) }
    var porDescargar by remember(siniestro?.id) { mutableStateOf<Evidencia?>(null) }
    val cerrado = siniestro?.etapa == EtapaSiniestro.CERRADO

    LaunchedEffect(siniestro?.id) { onCargar() }

    fun avisar(mensaje: String) {
        scope.launch { aviso.showSnackbar(mensaje) }
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto == null) avisar("Captura cancelada")
        else scope.launch {
            avisar(onGuardarFoto(tipoElegido, foto) ?: "Documento adjuntado al caso")
        }
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) camara.launch(null)
        else scope.launch {
            val accion = aviso.showSnackbar(
                message = "Sin permiso de cámara no se puede registrar la inspección",
                actionLabel = "Ajustes",
                duration = SnackbarDuration.Long
            )
            if (accion == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(contexto)
        }
    }
    val guardarComo = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { destino ->
        val elegida = porDescargar
        porDescargar = null
        if (destino == null) avisar("Descarga cancelada")
        else if (elegida != null) scope.launch {
            avisar(onDescargar(elegida.id, destino) ?: "Documento guardado en el teléfono")
        }
    }
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) avisar("Selección cancelada")
        else scope.launch {
            avisar(onGuardarArchivo(tipoElegido, uri) ?: "Documento adjuntado al caso")
        }
    }

    imagenVista?.let { imagen ->
        AlertDialog(
            onDismissRequest = { imagenVista = null },
            confirmButton = {
                TextButton(onClick = { imagenVista = null }) { Text("Cerrar") }
            },
            text = {
                Image(
                    bitmap = imagen.asImageBitmap(),
                    contentDescription = "Documento del caso",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    porEliminar?.let { objetivo ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar documento") },
            text = {
                Text(
                    "Se quitará \"" + objetivo.tipoDocumento + "\" del expediente del caso."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val error = onEliminar(objetivo.id)
                    porEliminar = null
                    avisar(error ?: "Documento eliminado")
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior("Documentos del caso", onVolver = onVolver) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (siniestro == null) {
                Text("El caso no está disponible.")
                return@Column
            }

            CodigoSiniestro(siniestro.numero, estilo = MaterialTheme.typography.titleLarge)
            Text("Etapa: " + siniestro.etapa.etiqueta)

            if (cerrado) {
                Text(
                    "El caso está cerrado. Solo puedes revisar los documentos.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text("Tipo de documento", style = MaterialTheme.typography.titleMedium)
                DocumentosLiquidador.tipos.forEach { tipo ->
                    FilterChip(
                        selected = tipoElegido == tipo,
                        onClick = { tipoElegido = tipo },
                        label = { Text(tipo) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (Permisos.tiene(contexto, Permisos.CAMARA)) camara.launch(null)
                            else permisoCamara.launch(Permisos.CAMARA)
                        }
                    ) { Text("Cámara") }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { galeria.launch("*/*") }
                    ) { Text("Archivo") }
                }
            }

            Text(
                "Documentos del caso (" + evidencias.size + ")",
                style = MaterialTheme.typography.titleMedium
            )
            if (evidencias.isEmpty()) Text("Todavía no hay documentos en este caso.")
            evidencias.forEach { evidencia ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        FilaDocumento(
                            tipo = evidencia.tipoDocumento,
                            nombre = evidencia.nombreArchivo,
                            detalle = evidencia.fechaRegistro + " · " +
                                (evidencia.tamanoBytes / 1024) + " KB",
                            esImagen = evidencia.tipoMime.startsWith("image/"),
                            cargarMiniatura = { onCargarMiniatura(evidencia.id) },
                            onAbrir = {
                                scope.launch {
                                    imagenVista = onCargarImagen(evidencia.id)
                                    if (imagenVista == null) avisar("No se pudo abrir la imagen")
                                }
                            },
                            onDescargar = {
                                porDescargar = evidencia
                                guardarComo.launch(evidencia.nombreArchivo)
                            },
                            onEliminar = if (cerrado ||
                                evidencia.tipoDocumento !in DocumentosLiquidador.tipos
                            ) null else { { porEliminar = evidencia } }
                        )
                    }
                }
            }
        }
        AvisoSuperior(aviso)
        }
    }
}
