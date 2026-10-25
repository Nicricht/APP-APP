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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.TextoPresentacion
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos
import kotlinx.coroutines.launch

enum class PestanaLiquidador(val titulo: String) {
    RESUMEN("Resumen"),
    MIS_CASOS("Mis casos"),
    GESTIONES("Gestiones"),
    DOCUMENTOS("Documentos")
}

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
    onCerrarAnticipado: (Long, String, String) -> String?,
    onVolver: () -> Unit,
    disponibles: List<Siniestro> = emptyList(),
    seguimientosVencidos: Set<Long> = emptySet(),
    onTomarCaso: (Long) -> String? = { "No disponible" },
    permitirElegirEquipo: Boolean = true,
    onCerrarSesion: () -> Unit = {},
    onVerPerfil: () -> Unit = {},
    onAbrirMenu: () -> Unit = {}
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var casoId by rememberSaveable(liquidador) { mutableStateOf<Long?>(null) }
    var pestaña by rememberSaveable(liquidador) { mutableStateOf(PestanaLiquidador.RESUMEN.name) }
    val estadosFormularios = rememberSaveableStateHolder()
    val actual = PestanaLiquidador.valueOf(pestaña)
    val caso = casos.firstOrNull { it.id == casoId }

    LaunchedEffect(casoId, liquidador) {
        casoId?.let(onCargarGestiones)
    }
    fun mostrar(mensaje: String) { scope.launch { aviso.showSnackbar(mensaje) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de siniestros") },
                navigationIcon = {
                    IconButton(onClick = onAbrirMenu) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = "Abrir menú de navegación"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(Modifier.fillMaxSize().padding(margen)) {
            ScrollableTabRow(selectedTabIndex = actual.ordinal, edgePadding = 8.dp) {
                PestanaLiquidador.entries.forEach { opcion ->
                    Tab(
                        selected = opcion == actual,
                        onClick = { pestaña = opcion.name },
                        text = { Text(opcion.titulo) },
                        icon = {
                            Icon(
                                painter = painterResource(when (opcion) {
                                    PestanaLiquidador.RESUMEN -> R.drawable.ic_estadisticas
                                    PestanaLiquidador.MIS_CASOS -> R.drawable.ic_casos
                                    PestanaLiquidador.GESTIONES -> R.drawable.ic_reportar
                                    PestanaLiquidador.DOCUMENTOS -> R.drawable.ic_documentos
                                }),
                                contentDescription = null
                            )
                        }
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (permitirElegirEquipo) {
                    TextButton(onClick = onVolver) { Text("Volver a los asegurados") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Liquidador.entries.forEach { equipo ->
                            FilterChip(
                                selected = liquidador == equipo,
                                onClick = {
                                    casoId = null
                                    pestaña = PestanaLiquidador.RESUMEN.name
                                    onSeleccionarLiquidador(equipo)
                                },
                                label = { Text(equipo.equipo) }
                            )
                        }
                    }
                }
                when (actual) {
                    PestanaLiquidador.RESUMEN -> {
                        Text("Área de " + liquidador.equipo,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Responsable: " + liquidador.nombre)
                        ResumenGraficosCasos(casos = casos, equipo = liquidador.equipo)
                        Text("Casos disponibles", style = MaterialTheme.typography.titleLarge)
                        if (disponibles.isEmpty()) {
                            Text("No hay siniestros pendientes de asignación en tu área.")
                        }
                        disponibles.forEach { disponible ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(disponible.numero, style = MaterialTheme.typography.titleMedium)
                                    Text(disponible.tipo.etiqueta)
                                    Text("Recibido · Sin liquidador asignado")
                                    Button(onClick = {
                                        val error = onTomarCaso(disponible.id)
                                        mostrar(error ?: "Caso asignado a tu equipo")
                                        if (error == null) {
                                            casoId = disponible.id
                                            pestaña = PestanaLiquidador.GESTIONES.name
                                        }
                                    }) { Text("Tomar caso") }
                                }
                            }
                        }
                    }
                    PestanaLiquidador.MIS_CASOS -> {
                        Text("Mis casos asignados", style = MaterialTheme.typography.titleLarge)
                        if (casos.isEmpty()) Text("No hay casos asignados.")
                        casos.forEach { siniestro ->
                            Card(
                                onClick = {
                                    casoId = siniestro.id
                                    pestaña = PestanaLiquidador.GESTIONES.name
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(siniestro.numero, style = MaterialTheme.typography.titleMedium)
                                    Text(siniestro.tipo.etiqueta)
                                    Text("Etapa: " + siniestro.etapa.etiqueta)
                                    if (siniestro.id in seguimientosVencidos) {
                                        Text("Seguimiento pendiente: registra una gestión visible",
                                            color = MaterialTheme.colorScheme.error)
                                    }
                                    Text("Seleccionar caso", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    PestanaLiquidador.GESTIONES, PestanaLiquidador.DOCUMENTOS -> {
                        if (caso == null) {
                            Text("Selecciona primero un caso en Mis casos.")
                            Button(onClick = { pestaña = PestanaLiquidador.MIS_CASOS.name }) {
                                Text("Elegir un caso")
                            }
                        } else {
                            TextButton(onClick = { pestaña = PestanaLiquidador.MIS_CASOS.name }) {
                                Text("Cambiar de caso")
                            }
                            Text(caso.numero, style = MaterialTheme.typography.titleLarge)
                            Text("Etapa actual: " + caso.etapa.etiqueta)
                            if (caso.id in seguimientosVencidos) {
                                Text("Seguimiento pendiente: registra una gestión visible",
                                    color = MaterialTheme.colorScheme.error)
                            }
                            if (actual == PestanaLiquidador.GESTIONES) {
                                estadosFormularios.SaveableStateProvider("gestiones-" + caso.id) {
                                    GestionesDelCaso(
                                        caso = caso,
                                        gestiones = gestiones,
                                        solicitudes = solicitudes,
                                        onRegistrarGestion = onRegistrarGestion,
                                        onAvanzar = onAvanzar,
                                        onCerrarAnticipado = onCerrarAnticipado,
                                        mostrar = ::mostrar
                                    )
                                }
                            } else {
                                estadosFormularios.SaveableStateProvider("documentos-" + caso.id) {
                                    DocumentosDelCaso(
                                        caso = caso,
                                        solicitudes = solicitudes,
                                        onSolicitarDocumento = onSolicitarDocumento,
                                        mostrar = ::mostrar
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

@Composable
private fun GestionesDelCaso(
    caso: Siniestro,
    gestiones: List<Gestion>,
    solicitudes: List<SolicitudDocumento>,
    onRegistrarGestion: (Long, String, Boolean) -> String?,
    onAvanzar: (Long, String, String) -> String?,
    onCerrarAnticipado: (Long, String, String) -> String?,
    mostrar: (String) -> Unit
) {
    var descripcion by rememberSaveable(caso.id) { mutableStateOf("") }
    var visible by rememberSaveable(caso.id) { mutableStateOf(true) }
    var resultado by rememberSaveable(caso.id) { mutableStateOf("") }
    var mensajeCierre by rememberSaveable(caso.id) { mutableStateOf("") }

    if (caso.etapa == EtapaSiniestro.CERRADO) {
        Text("Este siniestro está cerrado. No se admiten modificaciones.")
        Text("Resultado: " + (caso.resultadoCierre ?: "No disponible"))
        Text(caso.mensajeCierre ?: "Sin mensaje de cierre registrado")
    } else {
        Text("Registrar una gestión", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción de gestión") },
            modifier = Modifier.fillMaxWidth(),
            isError = descripcion.isNotEmpty() && ReglasEtapa.validarGestion(descripcion) != null,
            supportingText = { Text("Obligatorio, hasta 500 caracteres") },
            minLines = 2
        )
        TextButton(onClick = { descripcion = "Sin novedades"; visible = true }) {
            Text("Completar con Sin novedades")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(checked = visible, onCheckedChange = { visible = it })
            Text(if (visible) "Visible para el asegurado" else "Gestión interna",
                modifier = Modifier.padding(top = 14.dp))
        }
        Button(
            enabled = ReglasEtapa.validarGestion(descripcion) == null,
            onClick = {
                val error = onRegistrarGestion(caso.id, descripcion, visible)
                mostrar(error ?: "Gestión registrada")
                if (error == null) descripcion = ""
            }
        ) { Text("Registrar gestión") }

        if (caso.etapa == EtapaSiniestro.RECIBIDO ||
            caso.etapa == EtapaSiniestro.EVALUACION
        ) {
            Text("Cierre anticipado", style = MaterialTheme.typography.titleMedium)
            Text("Registra el motivo y la decisión correspondiente.")
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
                label = { Text("Mensaje de cierre obligatorio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                enabled = ReglasEtapa.validar(caso.etapa, EtapaSiniestro.CERRADO,
                    solicitudes.count { it.pendiente }, resultado, mensajeCierre) == null,
                onClick = {
                    val error = onCerrarAnticipado(caso.id, resultado, mensajeCierre)
                    mostrar(error ?: "Caso cerrado")
                    if (error == null) { resultado = ""; mensajeCierre = "" }
                }
            ) { Text("Confirmar cierre anticipado") }
        }
        val siguiente = ReglasEtapa.siguiente(caso.etapa)
        if (siguiente != null) {
            Text("Avanzar de etapa", style = MaterialTheme.typography.titleMedium)
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
                    minLines = 2
                )
            }
            val pendientes = solicitudes.count { it.pendiente }
            if (siguiente == EtapaSiniestro.LIQUIDACION && pendientes > 0) {
                Text("Faltan $pendientes documentos. Deben registrarse antes de avanzar.",
                    color = MaterialTheme.colorScheme.error)
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
    Text("Historial de gestiones", style = MaterialTheme.typography.titleMedium)
    if (gestiones.isEmpty()) Text("Sin gestiones registradas.")
    gestiones.forEach { gestion ->
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(gestion.fecha)
                Text(TextoPresentacion.gestion(gestion.descripcion))
                Text(TextoPresentacion.responsable(gestion.responsable) +
                    if (gestion.visibleAsegurado) " · Visible al asegurado" else " · Interna")
            }
        }
    }
}

@Composable
private fun DocumentosDelCaso(
    caso: Siniestro,
    solicitudes: List<SolicitudDocumento>,
    onSolicitarDocumento: (Long, String, String) -> String?,
    mostrar: (String) -> Unit
) {
    var tipoSolicitud by rememberSaveable(caso.id) { mutableStateOf("") }
    var comentarioSolicitud by rememberSaveable(caso.id) { mutableStateOf("") }
    if (caso.etapa != EtapaSiniestro.CERRADO) {
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
                mostrar(error ?: "Solicitud registrada en el expediente local")
                if (error == null) {
                    tipoSolicitud = ""
                    comentarioSolicitud = ""
                }
            }
        ) { Text("Registrar solicitud") }
    }
    Text("Documentos solicitados", style = MaterialTheme.typography.titleMedium)
    if (solicitudes.isEmpty()) Text("No hay documentos solicitados.")
    solicitudes.forEach { solicitud ->
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(solicitud.tipo, style = MaterialTheme.typography.titleSmall)
                Text(if (solicitud.pendiente) "Pendiente" else "Documento registrado")
                Text("Solicitado: " + solicitud.fechaSolicitud)
                if (solicitud.comentario.isNotBlank()) Text(solicitud.comentario)
            }
        }
    }
}
