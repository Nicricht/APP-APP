package cl.duocuc.segurosbpo.ui.screens

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.EvaluacionCierre
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.ValidadorDesistimiento
import cl.duocuc.segurosbpo.model.ValidadorEvaluacion
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSiniestroScreen(
    siniestro: Siniestro?,
    solicitudes: List<SolicitudDocumento>,
    consultasPendientes: Int,
    evaluacion: EvaluacionCierre?,
    onCargarSolicitudes: () -> Unit,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onEvidencias: () -> Unit,
    onHistorial: () -> Unit,
    onConsultas: () -> Unit,
    onDesistir: (String) -> String?,
    onGuardarEvaluacion: (Int, String) -> String?
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var motivoDesistir by rememberSaveable(siniestro?.id) { mutableStateOf("") }
    var pidiendoDesistir by remember(siniestro?.id) { mutableStateOf(false) }
    var nota by rememberSaveable(siniestro?.id) { mutableStateOf(0) }
    var comentario by rememberSaveable(siniestro?.id) { mutableStateOf("") }

    LaunchedEffect(siniestro?.id) {
        if (siniestro != null) onCargarSolicitudes()
    }

    if (pidiendoDesistir && siniestro != null) {
        val error = ValidadorDesistimiento.error(siniestro.etapa, motivoDesistir)
        AlertDialog(
            onDismissRequest = { pidiendoDesistir = false },
            title = { Text("Desistir del siniestro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("El caso quedará cerrado y no podrás reactivarlo desde la aplicación.")
                    OutlinedTextField(
                        value = motivoDesistir,
                        onValueChange = { motivoDesistir = it },
                        label = { Text("Motivo") },
                        minLines = 2,
                        isError = motivoDesistir.isNotEmpty() && error != null,
                        supportingText = { error?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = error == null,
                    onClick = {
                        val resultado = onDesistir(motivoDesistir)
                        pidiendoDesistir = false
                        if (resultado == null) motivoDesistir = ""
                        scope.launch {
                            aviso.showSnackbar(resultado ?: "Desistimiento registrado")
                        }
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { pidiendoDesistir = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior("Detalle del siniestro", onVolver = onVolver) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (siniestro == null) {
                Text("No se encontró el siniestro de este asegurado")
                return@Column
            }

            val pendientes = solicitudes.filter { it.pendiente }
            val cerrado = siniestro.etapa == EtapaSiniestro.CERRADO
            val plazo = ControlPlazos.resumenEtapa(
                siniestro.ultimaActualizacion,
                siniestro.compania.plazoDiasHabiles
            )

            CodigoSiniestro(siniestro.numero, estilo = MaterialTheme.typography.headlineSmall)

            BloqueDetalle("Estado del caso") {
                DatoDetalle("Etapa", siniestro.etapa.etiqueta)
                DatoDetalle("Compañía", siniestro.compania.etiqueta)
                DatoDetalle("Tipo", siniestro.tipo.etiqueta)
                DatoDetalle(
                    "Liquidador",
                    siniestro.liquidadorNombre
                        ?: siniestro.liquidadorAsignado?.let {
                            "Equipo " + it.equipo + ", por asignar"
                        }
                        ?: "Pendiente de asignación"
                )
                DatoDetalle("Última actualización", siniestro.ultimaActualizacion)
            }

            if (!cerrado) {
                BloqueDetalle("Tiempo en esta etapa") {
                    LinearProgressIndicator(
                        progress = {
                            if (plazo.plazoDiasHabiles == 0) 0f
                            else minOf(
                                1f,
                                plazo.diasHabiles.toFloat() / plazo.plazoDiasHabiles
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DatoDetalle(
                        "Días hábiles transcurridos",
                        plazo.diasHabiles.toString() + " de " + plazo.plazoDiasHabiles
                    )
                    Text(
                        if (plazo.excedido)
                            "La etapa superó el plazo estimado por " +
                                siniestro.compania.etiqueta + ". Puedes enviar una consulta."
                        else "Quedan " + plazo.diasRestantes +
                            " día(s) hábil(es) del plazo estimado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (plazo.excedido) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            BloqueDetalle("Datos del siniestro") {
                DatoDetalle("Ocurrencia", siniestro.fechaOcurrencia)
                DatoDetalle("Reporte", siniestro.fechaReporte)
                DatoDetalle("Comuna", siniestro.comuna)
                DatoDetalle("Terceros involucrados", if (siniestro.huboTerceros) "Sí" else "No")
                Text(siniestro.descripcion, style = MaterialTheme.typography.bodyMedium)
                if (ValidadorSiniestro.reporteFueraDePlazo(
                        siniestro.fechaOcurrencia, siniestro.fechaReporte
                    )
                ) {
                    Text(
                        "Reporte fuera de plazo (más de 30 días desde el siniestro)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            BloqueDetalle("Qué sigue") {
                if (pendientes.isNotEmpty()) {
                    Text(
                        if (pendientes.size == 1) "Tienes 1 documento pendiente por entregar."
                        else "Tienes " + pendientes.size + " documentos pendientes por entregar.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                    pendientes.forEach { solicitud ->
                        Text("· " + solicitud.tipo, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    if (pendientes.isNotEmpty())
                        "Adjunta los documentos solicitados para que tu caso pueda continuar."
                    else when (siniestro.etapa) {
                        EtapaSiniestro.RECIBIDO ->
                            "Estamos revisando tu reporte para asignarlo a un liquidador."
                        EtapaSiniestro.EVALUACION ->
                            "El liquidador está revisando tu caso. Te avisaremos si necesitamos documentos."
                        EtapaSiniestro.LIQUIDACION ->
                            "Estamos definiendo la resolución con tu compañía de seguros."
                        EtapaSiniestro.CERRADO ->
                            "Tu caso terminó. Revisa el resultado registrado."
                    }
                )
            }

            BloqueDetalle("Documentos habituales para este tipo de caso") {
                siniestro.tipo.documentosHabituales.forEach { documento ->
                    Text("· " + documento, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "El liquidador puede solicitar otros documentos según la revisión.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (cerrado) {
                BloqueDetalle("Resultado") {
                    DatoDetalle("Resultado", siniestro.resultadoCierre ?: "No informado")
                    Text(
                        siniestro.mensajeCierre ?: "Sin mensaje de cierre",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                BloqueDetalle("Evaluación de la atención") {
                    if (evaluacion != null) {
                        DatoDetalle("Nota registrada", evaluacion.nota.toString() + " de 5")
                        if (!evaluacion.comentario.isNullOrBlank()) {
                            Text(evaluacion.comentario, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            "Evaluado el " + evaluacion.fecha,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            "¿Cómo evalúas la atención de tu caso?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { valor ->
                                FilterChip(
                                    selected = nota == valor,
                                    onClick = { nota = valor },
                                    label = { Text(valor.toString()) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it },
                            label = { Text("Comentario (opcional)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ValidadorEvaluacion.error(nota, comentario) == null,
                            onClick = {
                                val error = onGuardarEvaluacion(nota, comentario)
                                scope.launch {
                                    aviso.showSnackbar(error ?: "Gracias por tu evaluación")
                                }
                            }
                        ) { Text("Enviar evaluación") }
                    }
                }
            }

            BloqueDetalle("Acciones") {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onEvidencias
                ) { Text("Documentos y evidencias") }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConsultas
                ) {
                    Text(
                        if (consultasPendientes > 0)
                            "Consultas (" + consultasPendientes + " sin responder)"
                        else "Consultas del caso"
                    )
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onHistorial
                ) { Text("Historial de gestiones") }
                if (siniestro.etapa == EtapaSiniestro.RECIBIDO) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onEditar
                    ) { Text("Corregir descripción") }
                }
                if (!cerrado) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { pidiendoDesistir = true }
                    ) {
                        Text("Desistir del siniestro", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        AvisoSuperior(aviso)
        }
    }
}

@Composable
private fun BloqueDetalle(titulo: String, contenido: @Composable () -> Unit) {
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

@Composable
private fun DatoDetalle(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
