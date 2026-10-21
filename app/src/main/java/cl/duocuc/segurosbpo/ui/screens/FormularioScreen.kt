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
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.BotonSecundario
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.components.CasillaConTexto
import cl.duocuc.segurosbpo.ui.components.GrupoRadio
import cl.duocuc.segurosbpo.ui.components.ImagenEncabezado
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
    viewModel: FormularioViewModel = viewModel()
){
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
                val numero = onRegistroExitoso(viewModel.uiState.value)
                viewModel.confirmarRegistro(numero)
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
            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = stringResource(R.string.cd_banner),
                titulo = stringResource(R.string.banner_titulo),
                subtitulo = stringResource(R.string.baner_subtitulo)
            )
            Column(
                modifier = Modifier.padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                androidx.compose.material3.TextButton(onClick = onVolver) { Text("Volver a mis siniestros") }
                //Card 1: Quien reporta
                TarjetaSeccion(titulo = stringResource(R.string.seccion_datos)) {
                    Text(
                        text = stringResource(R.string.etiqueta_asegurado),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(uiState.asegurado.etiqueta, style = MaterialTheme.typography.titleMedium)

                    //RNF-10: el RUT se muestra parcialmente oculto
                    Text(
                        text = stringResource(R.string.rut_asegurado, uiState.asegurado.rutOculto),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                //Card 2: Que paso
                TarjetaSeccion(titulo = stringResource(R.string.seccion_que_paso)) {
                    Text(
                        text = stringResource(R.string.etiqueta_compania),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    GrupoRadio(
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

                    Text(
                        text = stringResource(R.string.etiqueta_tipo),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    GrupoRadio(
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
                        error = uiState.errorDescripcion
                    )

                    //solo para siniestros de vehiculo (Aclaracion N 4)
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

                //Card 3: Donde y cuando
                TarjetaSeccion(titulo = stringResource(R.string.seccion_donde_cuando)) {
                    CampoTexto(
                        valor = uiState.fechaOcurrencia,
                        onValorCambia = onFechaCambia,
                        etiqueta = stringResource(R.string.campo_fecha),
                        icono = R.drawable.ic_check,
                        error = uiState.errorFecha,
                        tipoTeclado = KeyboardType.Number
                    )
                    CampoTexto(
                        valor = uiState.comuna,
                        onValorCambia = onComunaCambia,
                        etiqueta = stringResource(R.string.campo_comuna),
                        icono = R.drawable.ic_ubicacion,
                        error = uiState.errorComuna
                    )
                }

                //confirmacion de los datos
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

                //botones
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

                //mensaje de exito con el numero de siniestro (RF-03)
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
            //Aclaracion N 2: que viene ahora para el asegurado
            Text(
                text = stringResource(R.string.proximo_paso_recibido),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.espacioChico)
            )
        }
    }
}
