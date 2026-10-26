package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.permisos.Permisos
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import kotlinx.coroutines.launch

private data class AdjuntoPendiente(
    val clave: Long,
    val tipo: String,
    val nombre: String,
    val foto: Bitmap? = null,
    val archivo: Uri? = null,
    val esImagen: Boolean = false
)

internal fun previsualizable(mime: String): Boolean =
    mime.startsWith("image/") || mime == "application/pdf"

internal fun miniaturaDeAdjunto(contexto: Context, uri: Uri): Bitmap? {
    val mime = contexto.contentResolver.getType(uri).orEmpty()
    return when {
        mime.startsWith("image/") -> miniaturaDeUri(contexto, uri)
        mime == "application/pdf" -> miniaturaDePdfUri(contexto, uri)
        else -> null
    }
}

internal fun miniaturaDePdfUri(contexto: Context, uri: Uri): Bitmap? = runCatching {
    contexto.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
        android.graphics.pdf.PdfRenderer(descriptor).use { lector ->
            if (lector.pageCount == 0) return@use null
            lector.openPage(0).use { pagina ->
                val factor = 240f / maxOf(pagina.width, pagina.height)
                val ancho = (pagina.width * factor).toInt().coerceAtLeast(1)
                val alto = (pagina.height * factor).toInt().coerceAtLeast(1)
                val lienzo = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
                lienzo.eraseColor(android.graphics.Color.WHITE)
                pagina.render(
                    lienzo, null, null,
                    android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )
                lienzo
            }
        }
    }
}.getOrNull()

internal fun esImagen(contexto: Context, uri: Uri): Boolean =
    previsualizable(contexto.contentResolver.getType(uri).orEmpty())

internal fun miniaturaDeUri(contexto: Context, uri: Uri): Bitmap? = runCatching {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contexto.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, medidas)
    }
    var escala = 1
    while (medidas.outWidth / escala > 240 || medidas.outHeight / escala > 240) {
        escala *= 2
    }
    val opciones = BitmapFactory.Options().apply {
        inSampleSize = escala
        inPreferredConfig = Bitmap.Config.RGB_565
    }
    contexto.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, opciones)
    }
}.getOrNull()

private fun nombreDeArchivo(contexto: Context, uri: Uri): String {
    var nombre = "documento"
    runCatching {
        contexto.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { if (it.moveToFirst()) nombre = it.getString(0) ?: nombre }
    }
    return nombre
}

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
    onCargarMiniatura: suspend (Long) -> Bitmap?,
    onGuardarFoto: suspend (String, Bitmap) -> String?,
    onGuardarArchivo: suspend (String, Uri) -> String?,
    onEliminar: (Long) -> String?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aviso = remember { SnackbarHostState() }
    var tipoDocumento by rememberSaveable(siniestro?.id) { mutableStateOf("") }
    var mostrarError by rememberSaveable(siniestro?.id) { mutableStateOf(false) }
    var imagenVista by remember(siniestro?.id) { mutableStateOf<Bitmap?>(null) }
    var porEliminar by remember(siniestro?.id) { mutableStateOf<Evidencia?>(null) }
    var porEnviar by remember(siniestro?.id) { mutableStateOf<List<AdjuntoPendiente>>(emptyList()) }
    var enviando by remember(siniestro?.id) { mutableStateOf(false) }
    var enviados by remember(siniestro?.id) { mutableStateOf(0) }

    LaunchedEffect(siniestro?.id, asegurado) { onCargar() }

    val cupoLibre = 15 - evidencias.size - porEnviar.size

    fun avisar(mensaje: String) {
        scope.launch { aviso.showSnackbar(mensaje) }
    }

    fun agregar(nuevos: List<AdjuntoPendiente>) {
        if (nuevos.isEmpty()) return
        if (nuevos.size > cupoLibre) {
            avisar("Solo puedes tener 15 documentos por siniestro")
            return
        }
        porEnviar = porEnviar + nuevos
    }

    fun validarTipo(accion: () -> Unit) {
        mostrarError = tipoDocumento.isBlank()
        if (!mostrarError) accion()
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto == null) avisar("Captura cancelada")
        else agregar(listOf(
            AdjuntoPendiente(
                clave = System.nanoTime(),
                tipo = tipoDocumento.trim(),
                nombre = "Fotografía tomada ahora",
                foto = foto
            )
        ))
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) camara.launch(null)
        else scope.launch {
            val accion = aviso.showSnackbar(
                message = "Sin permiso de cámara no se puede adjuntar la fotografía",
                actionLabel = "Ajustes",
                duration = SnackbarDuration.Long
            )
            if (accion == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) avisar("Selección cancelada")
        else agregar(uris.map { uri ->
            AdjuntoPendiente(
                clave = System.nanoTime() + uri.hashCode(),
                tipo = tipoDocumento.trim(),
                nombre = nombreDeArchivo(context, uri),
                archivo = uri,
                esImagen = esImagen(context, uri)
            )
        })
    }
    val documentos = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) avisar("Selección cancelada")
        else agregar(listOf(
            AdjuntoPendiente(
                clave = System.nanoTime(),
                tipo = tipoDocumento.trim(),
                nombre = nombreDeArchivo(context, uri),
                archivo = uri,
                esImagen = esImagen(context, uri)
            )
        ))
    }

    fun abrirCamara() {
        if (Permisos.tiene(context, Permisos.CAMARA)) camara.launch(null)
        else permisoCamara.launch(Permisos.CAMARA)
    }

    fun enviarPendientes() {
        scope.launch {
            enviando = true
            val fallidos = mutableListOf<AdjuntoPendiente>()
            var ultimoError: String? = null
            porEnviar.forEach { adjunto ->
                val error = when {
                    adjunto.foto != null -> onGuardarFoto(adjunto.tipo, adjunto.foto)
                    adjunto.archivo != null -> onGuardarArchivo(adjunto.tipo, adjunto.archivo)
                    else -> "No se pudo leer el adjunto"
                }
                if (error != null) {
                    fallidos.add(adjunto)
                    ultimoError = error
                }
            }
            val logrados = porEnviar.size - fallidos.size
            porEnviar = fallidos
            enviando = false
            if (logrados > 0) enviados = logrados
            if (ultimoError != null) aviso.showSnackbar(ultimoError)
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
                    contentDescription = "Documento del siniestro",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
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
                    "Se quitará \"" + objetivo.tipoDocumento + "\" (" +
                        objetivo.nombreArchivo + ") de este siniestro."
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

    Scaffold(topBar = { BarraSuperior("Documentos y evidencias") }) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (siniestro == null) {
                    Text("Este siniestro no pertenece al asegurado seleccionado")
                    return@Column
                }

                val cerrado = siniestro.etapa == EtapaSiniestro.CERRADO
                val pendientes = solicitudes.filter { it.pendiente }

                CodigoSiniestro(siniestro.numero, estilo = MaterialTheme.typography.titleLarge)

                if (enviados > 0) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Envío exitoso",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                if (enviados == 1) "Se envió 1 documento al liquidador."
                                else "Se enviaron " + enviados + " documentos al liquidador."
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = onVolver
                                ) { Text("Volver al siniestro") }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { enviados = 0 }
                                ) { Text("Enviar otro") }
                            }
                        }
                    }
                }

                if (!cerrado) {
                    BloqueEvidencias("Adjuntar documentos") {
                        if (pendientes.isNotEmpty()) {
                            Text(
                                "El liquidador te pidió:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            pendientes.forEach { solicitud ->
                                FilterChip(
                                    selected = tipoDocumento == solicitud.tipo,
                                    onClick = {
                                        tipoDocumento = solicitud.tipo
                                        mostrarError = false
                                    },
                                    label = { Text(solicitud.tipo) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        val habituales = siniestro.tipo.documentosHabituales.filter { documento ->
                            pendientes.none { it.tipo.equals(documento, ignoreCase = true) }
                        }
                        if (habituales.isNotEmpty()) {
                            Text(
                                "Para este tipo de siniestro se suelen pedir:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            habituales.forEach { documento ->
                                FilterChip(
                                    selected = tipoDocumento == documento,
                                    onClick = {
                                        tipoDocumento = documento
                                        mostrarError = false
                                    },
                                    label = { Text(documento) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        OutlinedTextField(
                            value = tipoDocumento,
                            onValueChange = { tipoDocumento = it; mostrarError = false },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Tipo de documento") },
                            placeholder = { Text("Ej.: fotografía del daño") },
                            isError = mostrarError,
                            supportingText = {
                                val calza = pendientes.any {
                                    it.tipo.equals(tipoDocumento.trim(), ignoreCase = true)
                                }
                                Text(
                                    when {
                                        mostrarError -> "Indica el tipo de documento"
                                        calza -> "Responde a lo que te pidió el liquidador"
                                        pendientes.isNotEmpty() && tipoDocumento.isNotBlank() ->
                                            "Se enviará como documento adicional, no cierra lo solicitado"
                                        else -> "Se permiten JPG, PNG, WEBP o PDF de hasta 5 MB"
                                    },
                                    color = if (!mostrarError && !calza &&
                                        pendientes.isNotEmpty() && tipoDocumento.isNotBlank()
                                    ) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true
                        )
                        Button(
                            enabled = cupoLibre > 0,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { validarTipo { abrirCamara() } }
                        ) { Text("Tomar fotografía") }
                        OutlinedButton(
                            enabled = cupoLibre > 0,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { validarTipo { galeria.launch("image/*") } }
                        ) { Text("Elegir de la galería") }
                        OutlinedButton(
                            enabled = cupoLibre > 0,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { validarTipo { documentos.launch("application/pdf") } }
                        ) { Text("Adjuntar PDF") }
                    }
                }

                if (porEnviar.isNotEmpty()) {
                    BloqueEvidencias("Listos para enviar (" + porEnviar.size + ")") {
                        porEnviar.forEach { adjunto ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var vista by remember(adjunto.clave) {
                                    mutableStateOf(adjunto.foto)
                                }
                                if (adjunto.foto == null && adjunto.esImagen) {
                                    LaunchedEffect(adjunto.clave) {
                                        vista = adjunto.archivo?.let {
                                            miniaturaDeAdjunto(context, it)
                                        }
                                    }
                                }
                                val imagen = vista
                                if (imagen != null) {
                                    Image(
                                        bitmap = imagen.asImageBitmap(),
                                        contentDescription = adjunto.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { imagenVista = imagen }
                                    )
                                } else if (adjunto.esImagen) {
                                    Box(
                                        Modifier.size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        adjunto.tipo,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        adjunto.nombre,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                TextButton(
                                    enabled = !enviando,
                                    onClick = {
                                        porEnviar = porEnviar.filter { it.clave != adjunto.clave }
                                    }
                                ) { Text("Quitar") }
                            }
                        }
                        Text(
                            "Toca una imagen para verla en grande antes de enviarla.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                enabled = !enviando,
                                onClick = { porEnviar = emptyList() }
                            ) { Text("Descartar") }
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !enviando,
                                onClick = { enviarPendientes() }
                            ) {
                                Text(if (enviando) "Enviando" else "Enviar")
                            }
                        }
                    }
                }

                BloqueEvidencias("Documentos enviados (" + evidencias.size + " de 15)") {
                    if (evidencias.isEmpty()) {
                        Text("Todavía no has enviado documentos de este siniestro.")
                    }
                    evidencias.forEachIndexed { indice, evidencia ->
                        if (indice > 0) HorizontalDivider()
                        FilaDocumento(
                            tipo = evidencia.tipoDocumento,
                            nombre = evidencia.nombreArchivo,
                            detalle = evidencia.fechaRegistro + " · " +
                                (evidencia.tamanoBytes / 1024) + " KB",
                            esImagen = previsualizable(evidencia.tipoMime),
                            cargarMiniatura = { onCargarMiniatura(evidencia.id) },
                            onAbrir = {
                                scope.launch {
                                    imagenVista = onCargarImagen(evidencia.id)
                                    if (imagenVista == null) {
                                        aviso.showSnackbar("No se pudo abrir la imagen")
                                    }
                                }
                            },
                            // RN-11: los enviados se conservan; solo se quitan los pendientes de envío.
                            onEliminar = null
                        )
                    }
                }
            }
            AvisoSuperior(aviso)
        }
    }
}

@Composable
internal fun FilaDocumento(
    tipo: String,
    nombre: String,
    detalle: String,
    esImagen: Boolean,
    cargarMiniatura: suspend () -> Bitmap?,
    onAbrir: () -> Unit,
    onDescargar: (() -> Unit)? = null,
    onEliminar: (() -> Unit)?
) {
    var miniatura by remember(tipo, nombre, detalle) { mutableStateOf<Bitmap?>(null) }
    var intentada by remember(tipo, nombre, detalle) { mutableStateOf(false) }
    if (esImagen) {
        LaunchedEffect(tipo, nombre, detalle) {
            miniatura = cargarMiniatura()
            intentada = true
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (esImagen) {
            val vista = miniatura
            if (vista == null) {
                Box(
                    Modifier.size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Image(
                    bitmap = vista.asImageBitmap(),
                    contentDescription = "Vista previa: " + tipo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAbrir)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(tipo, style = MaterialTheme.typography.titleSmall)
            Text(nombre, style = MaterialTheme.typography.bodySmall)
            Text(detalle, style = MaterialTheme.typography.bodySmall)
            if (esImagen && intentada && miniatura == null) {
                Text(
                    "No se pudo abrir la vista previa",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Column {
            if (onDescargar != null) {
                TextButton(onClick = onDescargar) { Text("Descargar") }
            }
            if (onEliminar != null) {
                TextButton(onClick = onEliminar) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun BloqueEvidencias(titulo: String, contenido: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}
