package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import android.app.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import cl.duocuc.segurosbpo.ui.components.LocalAccionesBarra
import java.util.Calendar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.components.CasillaConTexto
import cl.duocuc.segurosbpo.ui.components.SelectorOpciones
import cl.duocuc.segurosbpo.ui.components.InterruptorConTexto
import cl.duocuc.segurosbpo.ui.components.TarjetaSeccion
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.Exito
import cl.duocuc.segurosbpo.ui.theme.ExitoFondo
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import cl.duocuc.segurosbpo.permisos.Permisos
import kotlinx.coroutines.launch


@Composable
fun FormularioScreen(
    aseguradoActual: Asegurado,
    onRegistroExitoso: (FormularioUiState) -> String,
    onVolver: () -> Unit,
    onVerMisSiniestros: (Boolean) -> Unit,
    onGuardarFoto: suspend (String, Bitmap) -> String?,
    onGuardarArchivo: suspend (String, Uri) -> String?,
    onPosibleDuplicado: (FormularioUiState) -> String? = { null }
) {
    FormularioScreen(
        aseguradoActual,
        onRegistroExitoso,
        onVolver,
        onVerMisSiniestros,
        onGuardarFoto,
        onGuardarArchivo,
        viewModel(),
        onPosibleDuplicado
    )
}

@Composable
fun FormularioScreen(
    aseguradoActual: Asegurado,
    onRegistroExitoso: (FormularioUiState) -> String,
    onVolver: () -> Unit,
    onVerMisSiniestros: (Boolean) -> Unit,
    onGuardarFoto: suspend (String, Bitmap) -> String?,
    onGuardarArchivo: suspend (String, Uri) -> String?,
    viewModel: FormularioViewModel,
    onPosibleDuplicado: (FormularioUiState) -> String? = { null }
){
    var duplicado by remember { mutableStateOf<String?>(null) }
    var adjuntos by remember { mutableStateOf<List<AdjuntoReporte>>(emptyList()) }
    var yaAdjuntados by remember { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()

    fun enviarAdjuntos() {
        val pendientes = adjuntos
        if (pendientes.isEmpty()) return
        adjuntos = emptyList()
        yaAdjuntados = true
        alcance.launch {
            pendientes.forEach { adjunto ->
                when {
                    adjunto.foto != null -> onGuardarFoto(adjunto.tipo, adjunto.foto)
                    adjunto.archivo != null -> onGuardarArchivo(adjunto.tipo, adjunto.archivo)
                    else -> null
                }
            }
        }
    }
    if (duplicado != null) {
        AlertDialog(
            onDismissRequest = { duplicado = null },
            title = { Text("Posible siniestro duplicado") },
            text = { Text("Ya existe un caso abierto: " + duplicado +
                ". Si se trata de otro incidente, puedes continuar.") },
            confirmButton = {
                TextButton(onClick = {
                    val numero = onRegistroExitoso(viewModel.uiState.value)
                    viewModel.confirmarRegistro(numero)
                    enviarAdjuntos()
                    duplicado = null
                }) { Text("Continuar de todos modos") }
            },
            dismissButton = {
                TextButton(onClick = { duplicado = null }) { Text("Revisar datos") }
            }
        )
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(aseguradoActual) {
        viewModel.seleccionarAsegurado(aseguradoActual)
    }
    FormularioContent(
        uiState = uiState,
        onAseguradoSeleccionado = viewModel::onAseguradoSeleccionado,
        onFechaCambia = viewModel::onFechaCambia,
        onComunaCambia = viewModel::onComunaCambia,
        onDescripcionCambia = viewModel::onDescripcionCambia,
        onCompaniaSeleccionada = viewModel::onCompaniaSeleccionada,
        onTipoSeleccionado = viewModel::onTipoSeleccionado,
        onTercerosCambia = viewModel::onTercerosCambia,
        onDatosCorrectosCambia = viewModel::onDatosCorrectosCambia,
        onRegistrar = {
            if (viewModel.onRegistrar()) {
                val numeroExistente = onPosibleDuplicado(viewModel.uiState.value)
                if (numeroExistente != null) {
                    duplicado = numeroExistente
                } else {
                    val numero = onRegistroExitoso(viewModel.uiState.value)
                    viewModel.confirmarRegistro(numero)
                    enviarAdjuntos()
                }
            }
        },
        onLimpiar = {
            adjuntos = emptyList()
            yaAdjuntados = false
            viewModel.onLimpiar()
        },
        onVolver = onVolver,
        onVerMisSiniestros = onVerMisSiniestros,
        adjuntos = adjuntos,
        onAdjuntos = { adjuntos = it },
        yaAdjuntados = yaAdjuntados
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioContent(
    uiState: FormularioUiState,
    onAseguradoSeleccionado: (Asegurado) -> Unit,
    onFechaCambia: (String) -> Unit,
    onComunaCambia: (String) -> Unit,
    onDescripcionCambia: (String) -> Unit,
    onCompaniaSeleccionada: (Compania) -> Unit,
    onTipoSeleccionado: (TipoSiniestro) -> Unit,
    onTercerosCambia: (Boolean) -> Unit,
    onDatosCorrectosCambia: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onVolver: () -> Unit,
    onVerMisSiniestros: (Boolean) -> Unit,
    adjuntos: List<AdjuntoReporte> = emptyList(),
    onAdjuntos: (List<AdjuntoReporte>) -> Unit = {},
    yaAdjuntados: Boolean = false
){
    val contexto = LocalContext.current
    val temaOscuro = LocalAccionesBarra.current.temaOscuro
    val abrirSelectorFecha = {
        val calendario = Calendar.getInstance()
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply { isLenient = false }
        runCatching { formato.parse(uiState.fechaOcurrencia) }.getOrNull()?.let {
            calendario.time = it
        }
        DatePickerDialog(
            contexto,
            if (temaOscuro) R.style.SelectorFechaRuedaOscuro else R.style.SelectorFechaRueda,
            { _, anio, mes, dia ->
                onFechaCambia(String.format(Locale.ROOT, "%02d-%02d-%04d", dia, mes + 1, anio))
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_formulario),
                destacada = true
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().
            padding(innerPadding).
            imePadding().
            verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {

                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {
                    Text(
                        text = stringResource(R.string.etiqueta_asegurado),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(uiState.asegurado.etiqueta, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)


                    Text(
                        text = stringResource(R.string.rut_asegurado, uiState.asegurado.rutOculto),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }


                TarjetaSeccion(titulo = stringResource(R.string.seccion_que_paso)) {
                    SelectorOpciones(
                        titulo = stringResource(R.string.etiqueta_compania),
                        opciones = Compania.entries,
                        seleccionada = uiState.compania,
                        onSeleccionar = onCompaniaSeleccionada,
                        textoDe = {it.etiqueta}
                    )

                    uiState.errorCompania?.let {
                        mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    SelectorOpciones(
                        titulo = stringResource(R.string.etiqueta_tipo),
                        opciones = TipoSiniestro.entries,
                        seleccionada = uiState.tipoSiniestro,
                        onSeleccionar = onTipoSeleccionado,
                        textoDe = {it.etiqueta}
                    )

                    CampoTexto(
                        valor = uiState.descripcion,
                        onValorCambia = onDescripcionCambia,
                        etiqueta = stringResource(R.string.campo_descripcion),
                        icono = R.drawable.ic_correo,
                        multilinea = true,
                        error = uiState.errorDescripcion
                    )


                    if (uiState.tipoSiniestro.esVehiculo) {
                        InterruptorConTexto(
                            texto = stringResource(R.string.opcion_terceros),
                            activado = uiState.huboTerceros,
                            onCambio = onTercerosCambia
                        )

                        if (uiState.huboTerceros) {
                            Text(
                                text = stringResource(R.string.aviso_constancia),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }


                TarjetaSeccion(titulo = stringResource(R.string.seccion_donde_cuando)) {
                    CampoTexto(
                        valor = uiState.fechaOcurrencia,
                        onValorCambia = onFechaCambia,
                        etiqueta = stringResource(R.string.campo_fecha),
                        icono = R.drawable.ic_calendario,
                        error = uiState.errorFecha,
                        tipoTeclado = KeyboardType.Number,
                        onPulsar = { abrirSelectorFecha() }
                    )
                    if (ValidadorSiniestro.reporteFueraDePlazo(
                            uiState.fechaOcurrencia,
                            SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).format(Date())
                        )) {
                        Text(
                            text = "Reporte fuera de plazo: pasaron más de 30 días desde el siniestro. Puedes continuar con el reporte.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    CampoTexto(
                        valor = uiState.comuna,
                        onValorCambia = onComunaCambia,
                        etiqueta = stringResource(R.string.campo_comuna),
                        icono = R.drawable.ic_ubicacion,
                        error = uiState.errorComuna
                    )
                }


                TarjetaSeccion(titulo = "Documentos del siniestro") {
                    AdjuntosDelReporte(
                        sugerencias = uiState.tipoSiniestro.documentosHabituales,
                        adjuntos = adjuntos,
                        onAdjuntos = onAdjuntos,
                        habilitado = !uiState.reporteExitoso
                    )
                }


                Column {
                    CasillaConTexto(
                        texto = stringResource(R.string.opcion_datos_correctos),
                        marcado = uiState.datosCorrectos,
                        onCambio = onDatosCorrectosCambia
                    )
                    uiState.errorDatos?.let {
                        mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )

                    }
                }


                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                    BotonSecundario(
                        texto = stringResource(R.string.btn_limpiar),
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f)
                    )

                    BotonPrincipal(
                        texto = stringResource(R.string.btn_registrar),
                        onClick = onRegistrar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.weight(1f)
                    )
                }


                AnimatedVisibility(
                    visible = uiState.reporteExitoso,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        MensajeExito(numero = uiState.numeroSiniestro)
                        BotonPrincipal(
                            texto = if (yaAdjuntados) "Ver mis siniestros"
                                else "Adjuntar los documentos",
                            onClick = { onVerMisSiniestros(yaAdjuntados) },
                            icono = R.drawable.ic_check,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(Dimens.espacioGrande))
            }
        }
    }
}

@Composable
private fun  MensajeExito(numero: String){
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ExitoFondo,
            contentColor = Exito
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Exito,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
                Text(
                    text = stringResource(R.string.reporte_exitoso, numero),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = Dimens.espacioChico)
                )
            }

            Text(
                text = stringResource(R.string.proximo_paso_recibido),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.espacioChico)
            )
        }
    }
}

data class AdjuntoReporte(
    val clave: Long,
    val tipo: String,
    val nombre: String,
    val foto: Bitmap? = null,
    val archivo: Uri? = null,
    val esImagen: Boolean = false
)

@Composable
private fun AdjuntosDelReporte(
    sugerencias: List<String>,
    adjuntos: List<AdjuntoReporte>,
    onAdjuntos: (List<AdjuntoReporte>) -> Unit,
    habilitado: Boolean
) {
    val contexto = LocalContext.current
    var tipo by rememberSaveable { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var ampliada by remember { mutableStateOf<Bitmap?>(null) }

    ampliada?.let { imagen ->
        AlertDialog(
            onDismissRequest = { ampliada = null },
            confirmButton = {
                TextButton(onClick = { ampliada = null }) { Text("Cerrar") }
            },
            text = {
                Image(
                    bitmap = imagen.asImageBitmap(),
                    contentDescription = "Documento por enviar",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }

    fun agregar(nuevos: List<AdjuntoReporte>) {
        if (nuevos.isEmpty()) return
        if (adjuntos.size + nuevos.size > 15) {
            mensaje = "Puedes adjuntar hasta 15 documentos"
            return
        }
        mensaje = null
        onAdjuntos(adjuntos + nuevos)
        tipo = ""
    }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto ->
        if (foto != null) {
            agregar(listOf(AdjuntoReporte(
                System.nanoTime(), tipo.trim(), "Fotografía tomada ahora",
                foto = foto, esImagen = true
            )))
        }
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) camara.launch(null)
        else mensaje = "Sin permiso de cámara no se puede tomar la fotografía"
    }
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        agregar(uris.map { uri ->
            AdjuntoReporte(
                System.nanoTime() + uri.hashCode(), tipo.trim(),
                uri.lastPathSegment ?: "documento", archivo = uri,
                esImagen = esImagen(contexto, uri)
            )
        })
    }
    val pdf = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            agregar(listOf(AdjuntoReporte(
                System.nanoTime(), tipo.trim(), uri.lastPathSegment ?: "documento",
                archivo = uri, esImagen = true
            )))
        }
    }

    fun conTipo(accion: () -> Unit) {
        if (tipo.isBlank()) mensaje = "Indica qué documento estás adjuntando" else accion()
    }

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        Text(
            "Adjunta ahora lo que tengas. Si te falta algo, puedes enviarlo después.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        sugerencias.forEach { sugerencia ->
            FilterChip(
                selected = tipo == sugerencia,
                onClick = { tipo = sugerencia; mensaje = null },
                label = { Text(sugerencia) },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = tipo,
            onValueChange = { tipo = it; mensaje = null },
            label = { Text("Tipo de documento") },
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
            BotonSecundario(
                texto = "Cámara",
                onClick = {
                    conTipo {
                        if (Permisos.tiene(contexto, Permisos.CAMARA)) camara.launch(null)
                        else permisoCamara.launch(Permisos.CAMARA)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            BotonSecundario(
                texto = "Galería",
                onClick = { conTipo { galeria.launch("image/*") } },
                modifier = Modifier.weight(1f)
            )
            BotonSecundario(
                texto = "PDF",
                onClick = { conTipo { pdf.launch("application/pdf") } },
                modifier = Modifier.weight(1f)
            )
        }
        mensaje?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        adjuntos.forEach { adjunto ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var vista by remember(adjunto.clave) { mutableStateOf(adjunto.foto) }
                if (adjunto.foto == null && adjunto.esImagen) {
                    LaunchedEffect(adjunto.clave) {
                        vista = adjunto.archivo?.let { miniaturaDeAdjunto(contexto, it) }
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
                            .clickable { ampliada = imagen }
                    )
                } else if (adjunto.esImagen) {
                    Box(
                        Modifier.size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(adjunto.tipo, style = MaterialTheme.typography.titleSmall)
                    Text(adjunto.nombre, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(
                    enabled = habilitado,
                    onClick = { onAdjuntos(adjuntos.filter { it.clave != adjunto.clave }) }
                ) { Text("Quitar") }
            }
        }
        if (adjuntos.isNotEmpty()) {
            Text(
                "Se enviarán " + adjuntos.size + " documento(s) al registrar el reporte.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
