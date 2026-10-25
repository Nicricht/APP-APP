package cl.duocuc.segurosbpo.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.components.CasillaConTexto
import cl.duocuc.segurosbpo.ui.components.SelectorOpciones
import cl.duocuc.segurosbpo.ui.components.SelectorComuna
import cl.duocuc.segurosbpo.model.FechaFormulario
import cl.duocuc.segurosbpo.ui.components.InterruptorConTexto
import cl.duocuc.segurosbpo.ui.components.TarjetaSeccion
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.Exito
import cl.duocuc.segurosbpo.ui.theme.ExitoFondo
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import androidx.compose.runtime.getValue


@Composable
fun FormularioScreen(
    aseguradoActual: Asegurado,
    onRegistroExitoso: (FormularioUiState) -> String,
    onVolver: () -> Unit,
    onVerMisSiniestros: () -> Unit,
    onPosibleDuplicado: (FormularioUiState) -> String? = { null }
) {
    FormularioScreen(
        aseguradoActual,
        onRegistroExitoso,
        onVolver,
        onVerMisSiniestros,
        viewModel(),
        onPosibleDuplicado
    )
}

@Composable
fun FormularioScreen(
    aseguradoActual: Asegurado,
    onRegistroExitoso: (FormularioUiState) -> String,
    onVolver: () -> Unit,
    onVerMisSiniestros: () -> Unit,
    viewModel: FormularioViewModel,
    onPosibleDuplicado: (FormularioUiState) -> String? = { null }
){
    var duplicado by remember { mutableStateOf<String?>(null) }
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
                }
            }
        },
        onLimpiar = viewModel::onLimpiar,
        onVolver = onVolver,
        onVerMisSiniestros = onVerMisSiniestros
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
    onVerMisSiniestros: () -> Unit
){
    val contexto = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_formulario)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().
            padding(innerPadding).
            verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                androidx.compose.material3.TextButton(onClick = onVolver) { Text("Volver a mis siniestros") }

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
                        textoDe = {it.etiqueta},
                        placeholder = "Selecciona una compañía"
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
                        textoDe = {it.etiqueta},
                        placeholder = "Selecciona el tipo de siniestro"
                    )

                    uiState.errorTipo?.let { mensaje ->
                        Text(mensaje, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium)
                    }

                    CampoTexto(
                        valor = uiState.descripcion,
                        onValorCambia = onDescripcionCambia,
                        etiqueta = stringResource(R.string.campo_descripcion),
                        icono = R.drawable.ic_correo,
                        multilinea = true,
                        error = uiState.errorDescripcion
                    )


                    if (uiState.tipoSiniestro?.esVehiculo == true) {
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
                    OutlinedButton(
                        onClick = {
                            val inicial = Calendar.getInstance().apply {
                                val partes = uiState.fechaOcurrencia.split("-")
                                if (partes.size == 3) {
                                    runCatching { set(partes[2].toInt(), partes[1].toInt() - 1,
                                        partes[0].toInt()) }
                                }
                            }
                            val dialogo = android.app.DatePickerDialog(
                                contexto,
                                { _, anio, mes, dia ->
                                    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        clear()
                                        set(anio, mes, dia, 12, 0, 0)
                                    }
                                    onFechaCambia(FechaFormulario.formatear(utc.timeInMillis))
                                },
                                inicial.get(Calendar.YEAR), inicial.get(Calendar.MONTH),
                                inicial.get(Calendar.DAY_OF_MONTH)
                            )
                            dialogo.datePicker.maxDate = System.currentTimeMillis()
                            dialogo.show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_calendario), contentDescription = null)
                        Text(if (uiState.fechaOcurrencia.isBlank()) " Elegir fecha del siniestro"
                            else " Fecha: ${uiState.fechaOcurrencia}")
                    }
                    uiState.errorFecha?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
                    SelectorComuna(
                        comuna = uiState.comuna,
                        error = uiState.errorComuna,
                        onSeleccionar = onComunaCambia
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
                            texto = "Ver mis siniestros",
                            onClick = onVerMisSiniestros,
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
