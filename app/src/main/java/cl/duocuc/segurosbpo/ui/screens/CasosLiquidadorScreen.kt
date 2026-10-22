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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasosLiquidadorScreen(
    liquidador: Liquidador,
    casos: List<Siniestro>,
    gestiones: List<Gestion>,
    solicitudes: List<SolicitudDocumento>,
    onSolicitarDocumento: (Long, String, String) -> String?,
    onSeleccionarLiquidador: (Liquidador) -> Unit,
    onCargarGestiones: (Long) -> Unit,
    onRegistrarGestion: (Long, String, Boolean) -> String?,
    onAvanzar: (Long, String, String) -> String?,
    onVolver: () -> Unit
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var casoId by rememberSaveable(liquidador) { mutableStateOf<Long?>(null) }
    var descripcion by rememberSaveable(casoId) { mutableStateOf("") }
    var tipoSolicitud by rememberSaveable(casoId) { mutableStateOf("") }
    var comentarioSolicitud by rememberSaveable(casoId) { mutableStateOf("") }
    var visible by rememberSaveable(casoId) { mutableStateOf(true) }
    var resultado by rememberSaveable(casoId) { mutableStateOf("") }
    var mensajeCierre by rememberSaveable(casoId) { mutableStateOf("") }
    val caso = casos.firstOrNull { it.id == casoId }

    LaunchedEffect(casoId, liquidador) {
        if (casoId != null) onCargarGestiones(casoId!!)
    }
    fun mostrar(mensaje: String) { scope.launch { aviso.showSnackbar(mensaje) } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestión de siniestros") }) },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver a los asegurados") }
            Text("Perfil de liquidador (simulado)", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Liquidador.entries.forEach { persona ->
                    FilterChip(
                        selected = liquidador == persona,
                        onClick = { casoId = null; onSeleccionarLiquidador(persona) },
                        label = { Text(persona.equipo) }
                    )
                }
            }
            Text("Responsable: " + liquidador.nombre)
            Text("Solo casos asignados a este equipo.")
            ResumenGraficosCasos(casos = casos, equipo = liquidador.equipo)
            if (casos.isEmpty()) Text("No hay casos asignados.")
            if (caso == null) {
                casos.forEach { siniestro ->
                Card(onClick = { casoId = siniestro.id }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(siniestro.numero, style = MaterialTheme.typography.titleMedium)
                        Text(siniestro.tipo.etiqueta)
                        Text("Etapa: " + siniestro.etapa.etiqueta)
                    }
                }
                }
            }
            if (caso != null) {
                TextButton(onClick = { casoId = null }) { Text("Volver a la lista de casos") }
                Text("Gestionar: " + caso.numero, style = MaterialTheme.typography.titleLarge)
                Text("Etapa actual: " + caso.etapa.etiqueta)
                if (caso.etapa == EtapaSiniestro.CERRADO) {
                    Text("Este siniestro está cerrado. No se admiten modificaciones.")
                    Text("Resultado: " + (caso.resultadoCierre ?: "No disponible"))
                    Text(caso.mensajeCierre ?: "Sin mensaje de cierre registrado")
                } else {
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción de gestión") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = descripcion.isNotEmpty() && ReglasEtapa.validarGestion(descripcion) != null,
                        supportingText = { Text("Obligatorio, hasta 500 caracteres") },
                        minLines = 2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = visible, onCheckedChange = { visible = it })
                        Text(if (visible) "Visible para el asegurado" else "Gestión interna",
                            modifier = Modifier.padding(top = 14.dp))
                    }
                    Button(
                        enabled = ReglasEtapa.validarGestion(descripcion) == null,
                        onClick = {
                            val error = onRegistrarGestion(caso.id, descripcion, visible)
                            mostrar(error ?: "Gestión guardada")
                            if (error == null) descripcion = ""
                        }
                    ) { Text("Registrar gestión") }
                    Text("Solicitar documento", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = tipoSolicitud,
                        onValueChange = { tipoSolicitud = it },
                        label = { Text("Nombre del documento requerido") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = comentarioSolicitud,
                        onValueChange = { comentarioSolicitud = it },
                        label = { Text("Indicaciones para el asegurado (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Button(
                        enabled = tipoSolicitud.isNotBlank() && tipoSolicitud.length <= 100 &&
                            comentarioSolicitud.length <= 500,
                        onClick = {
                            val error = onSolicitarDocumento(caso.id, tipoSolicitud, comentarioSolicitud)
                            mostrar(error ?: "Documento solicitado")
                            if (error == null) {
                                tipoSolicitud = ""
                                comentarioSolicitud = ""
                            }
                        }
                    ) { Text("Solicitar documento") }
                    val siguiente = ReglasEtapa.siguiente(caso.etapa)
                    if (siguiente != null && caso.etapa != EtapaSiniestro.RECIBIDO) {
                        Text("Siguiente etapa: " + siguiente.etiqueta)
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
                                label = { Text("Mensaje de cierre obligatorio") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = mensajeCierre.isEmpty() && resultado.isNotEmpty()
                            )
                        }
                        val pendientes = solicitudes.count { it.pendiente }
                        if (siguiente == EtapaSiniestro.LIQUIDACION && pendientes > 0) {
                            Text(
                                "Faltan $pendientes documentos. Deben entregarse antes de avanzar.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Button(
                            enabled = ReglasEtapa.validar(caso.etapa, siguiente, pendientes,
                                resultado, mensajeCierre) == null,
                            onClick = {
                                val error = onAvanzar(caso.id, resultado, mensajeCierre)
                                mostrar(error ?: "Etapa actualizada")
                                if (error == null) { resultado = ""; mensajeCierre = "" }
                            }
                        ) { Text("Confirmar avance a " + siguiente.etiqueta) }
                    }
                }
                Text("Documentos solicitados", style = MaterialTheme.typography.titleMedium)
                if (solicitudes.isEmpty()) Text("No hay documentos solicitados.")
                solicitudes.forEach { solicitud ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(solicitud.tipo, style = MaterialTheme.typography.titleSmall)
                            Text(if (solicitud.pendiente) "Pendiente" else "Entregado localmente")
                            Text("Solicitado: " + solicitud.fechaSolicitud)
                        }
                    }
                }
                Text("Historial de gestiones", style = MaterialTheme.typography.titleMedium)
                if (gestiones.isEmpty()) Text("Sin gestiones registradas.")
                gestiones.forEach { gestion ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(gestion.fecha)
                            Text(gestion.descripcion)
                            Text(gestion.responsable + if (gestion.visibleAsegurado)
                                " · Visible al asegurado" else " · Interna")
                        }
                    }
                }
            }
        }
    }
}
