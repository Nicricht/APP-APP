package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import cl.duocuc.segurosbpo.model.Consulta
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.ValidadorConsulta
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCasoScreen(
    caso: Siniestro?,
    gestiones: List<Gestion>,
    solicitudes: List<SolicitudDocumento>,
    consultas: List<Consulta>,
    seguimientoVencido: Boolean,
    onCargar: () -> Unit,
    onRegistrarGestion: (Long, String, Boolean) -> String?,
    onSolicitarDocumento: (Long, String, String) -> String?,
    onAvanzar: (Long, String, String) -> String?,
    onCerrarAnticipado: (Long, String, String) -> String?,
    onResponderConsulta: (Long, String) -> String?,
    onMarcarRecibida: (Long) -> String?,
    onAnularSolicitud: (Long) -> String?,
    onVerDocumentos: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var descripcion by rememberSaveable(caso?.id) { mutableStateOf("") }
    var visible by rememberSaveable(caso?.id) { mutableStateOf(true) }
    var tipoSolicitud by rememberSaveable(caso?.id) { mutableStateOf("") }
    var comentarioSolicitud by rememberSaveable(caso?.id) { mutableStateOf("") }
    var resultado by rememberSaveable(caso?.id) { mutableStateOf("") }
    var mensajeCierre by rememberSaveable(caso?.id) { mutableStateOf("") }

    LaunchedEffect(caso?.id) { onCargar() }

    fun mostrar(mensaje: String) {
        scope.launch { aviso.showSnackbar(mensaje) }
    }

    Scaffold(
        topBar = {
            BarraSuperior(caso?.numero ?: "Gestión del caso", onVolver = onVolver)
        },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (caso == null) {
                Text("El caso no está disponible.")
                return@Column
            }

            val pendientes = solicitudes.count { it.pendiente }
            val cerrado = caso.etapa == EtapaSiniestro.CERRADO

            Seccion("Estado del caso") {
                CodigoSiniestro(caso.numero)
                Dato("Tipo", caso.tipo.etiqueta)
                Dato("Etapa actual", caso.etapa.etiqueta)
                Dato("Documentos pendientes", pendientes.toString())
                if (seguimientoVencido) {
                    Text(
                        "Seguimiento pendiente: registra una gestión visible",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onVerDocumentos(caso.id) }
                ) { Text("Documentos del caso") }
            }

            if (cerrado) {
                Seccion("Cierre") {
                    Dato("Resultado", caso.resultadoCierre ?: "No disponible")
                    Dato("Mensaje", caso.mensajeCierre ?: "Sin mensaje registrado")
                    Text(
                        "El caso está cerrado y no admite modificaciones.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Seccion("Registrar gestión") {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = descripcion.isNotEmpty() &&
                            ReglasEtapa.validarGestion(descripcion) != null,
                        supportingText = { Text("Obligatorio, hasta 500 caracteres") },
                        minLines = 2
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(checked = visible, onCheckedChange = { visible = it })
                        Text(
                            if (visible) "Visible para el asegurado" else "Gestión interna",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            descripcion = "Sin novedades"
                            visible = true
                        }) { Text("Sin novedades") }
                        Button(
                            enabled = ReglasEtapa.validarGestion(descripcion) == null,
                            onClick = {
                                val error = onRegistrarGestion(caso.id, descripcion, visible)
                                mostrar(error ?: "Gestión guardada")
                                if (error == null) descripcion = ""
                            }
                        ) { Text("Registrar") }
                    }
                }

                val siguiente = ReglasEtapa.siguiente(caso.etapa)
                if (siguiente != null) {
                    Seccion("Avanzar a " + siguiente.etiqueta) {
                        if (siguiente == EtapaSiniestro.CERRADO) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Aprobado", "Rechazado").forEach { opcion ->
                                    FilterChip(
                                        selected = resultado == opcion,
                                        onClick = { resultado = opcion },
                                        label = { Text(opcion) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = mensajeCierre,
                                onValueChange = { mensajeCierre = it },
                                label = { Text("Mensaje de cierre") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = mensajeCierre.isEmpty() && resultado.isNotEmpty()
                            )
                        }
                        if (siguiente == EtapaSiniestro.LIQUIDACION && pendientes > 0) {
                            Text(
                                if (pendientes == 1)
                                    "Falta 1 documento. Debe entregarse antes de avanzar."
                                else "Faltan $pendientes documentos. Deben entregarse antes de avanzar.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ReglasEtapa.validar(
                                caso.etapa, siguiente, pendientes, resultado, mensajeCierre
                            ) == null,
                            onClick = {
                                val error = onAvanzar(caso.id, resultado, mensajeCierre)
                                mostrar(error ?: "Etapa actualizada")
                                if (error == null) { resultado = ""; mensajeCierre = "" }
                            }
                        ) { Text("Confirmar avance") }
                    }
                }

                if (caso.etapa == EtapaSiniestro.RECIBIDO ||
                    caso.etapa == EtapaSiniestro.EVALUACION
                ) {
                    Seccion("Cierre anticipado") {
                        Text(
                            "Registra la decisión de la compañía o el desistimiento informado.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        val opciones = if (caso.etapa == EtapaSiniestro.RECIBIDO)
                            listOf("Desistido") else listOf("Rechazado", "Desistido")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            opciones.forEach { opcion ->
                                FilterChip(
                                    selected = resultado == opcion,
                                    onClick = { resultado = opcion },
                                    label = { Text(opcion) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = mensajeCierre,
                            onValueChange = { mensajeCierre = it },
                            label = { Text("Mensaje de cierre") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ReglasEtapa.validar(
                                caso.etapa, EtapaSiniestro.CERRADO, pendientes,
                                resultado, mensajeCierre
                            ) == null,
                            onClick = {
                                val error = onCerrarAnticipado(caso.id, resultado, mensajeCierre)
                                mostrar(error ?: "Caso cerrado")
                                if (error == null) { resultado = ""; mensajeCierre = "" }
                            }
                        ) { Text("Confirmar cierre") }
                    }
                }

                Seccion("Requerimientos al asegurado") {
                    OutlinedTextField(
                        value = tipoSolicitud,
                        onValueChange = { tipoSolicitud = it },
                        label = { Text("Documento requerido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = comentarioSolicitud,
                        onValueChange = { comentarioSolicitud = it },
                        label = { Text("Indicaciones (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = tipoSolicitud.isNotBlank() && tipoSolicitud.length <= 100 &&
                            comentarioSolicitud.length <= 500,
                        onClick = {
                            val error = onSolicitarDocumento(
                                caso.id, tipoSolicitud, comentarioSolicitud
                            )
                            mostrar(error ?: "Documento solicitado")
                            if (error == null) {
                                tipoSolicitud = ""
                                comentarioSolicitud = ""
                            }
                        }
                    ) { Text("Solicitar") }

                    if (solicitudes.isNotEmpty()) {
                        HorizontalDivider()
                        solicitudes.forEach { solicitud ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(solicitud.tipo, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    (if (solicitud.pendiente) "Pendiente"
                                        else "Entregado el " + solicitud.fechaEntrega.orEmpty()) +
                                        " · Solicitado el " + solicitud.fechaSolicitud,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (solicitud.pendiente) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(onClick = {
                                            val error = onMarcarRecibida(solicitud.id)
                                            mostrar(error ?: "Documento dado por recibido")
                                        }) { Text("Dar por recibido") }
                                        TextButton(onClick = {
                                            val error = onAnularSolicitud(solicitud.id)
                                            mostrar(error ?: "Solicitud anulada")
                                        }) {
                                            Text(
                                                "Anular",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Seccion("Consultas del asegurado") {
                val sinResponder = consultas.count { !it.respondida }
                Dato("Recibidas", consultas.size.toString())
                Dato("Sin responder", sinResponder.toString())
                if (consultas.isEmpty()) {
                    Text(
                        "La persona asegurada no ha enviado consultas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                consultas.forEach { consulta ->
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(consulta.pregunta, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            consulta.fechaPregunta,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (consulta.respondida) {
                            Text(
                                "Respuesta: " + consulta.respuesta.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else if (!cerrado) {
                            var respuesta by rememberSaveable(consulta.id) { mutableStateOf("") }
                            OutlinedTextField(
                                value = respuesta,
                                onValueChange = { respuesta = it },
                                label = { Text("Respuesta") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = ValidadorConsulta.errorRespuesta(respuesta) == null,
                                onClick = {
                                    val error = onResponderConsulta(consulta.id, respuesta)
                                    mostrar(error ?: "Respuesta enviada")
                                    if (error == null) respuesta = ""
                                }
                            ) { Text("Responder") }
                        } else {
                            Text(
                                "Quedó sin responder antes del cierre.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Seccion("Historial de gestiones") {
                if (gestiones.isEmpty()) Text("Sin gestiones registradas.")
                gestiones.forEach { gestion ->
                    Column {
                        Text(gestion.descripcion, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            gestion.fecha + " · " + gestion.responsable +
                                if (gestion.visibleAsegurado) " · Visible" else " · Interna",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
