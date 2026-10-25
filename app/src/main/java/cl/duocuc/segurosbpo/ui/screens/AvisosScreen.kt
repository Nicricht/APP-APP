package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.AvisoSiniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosScreen(
    avisos: List<AvisoSiniestro>,
    onCargar: () -> Unit,
    onMarcarLeido: (Long) -> Unit,
    onAbrirCaso: (Long) -> Unit,
    onAdjuntarDocumento: (Long) -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) { onCargar() }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    val filtrados = avisos.filter { aviso -> when (filtro) {
        "Pendientes" -> !aviso.leido
        "Leídos" -> aviso.leido
        else -> true
    } }
    Scaffold(topBar = { TopAppBar(title = { Text("Avisos") }) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver a mis siniestros") }
            Text("Mantente al día con tus siniestros",
                style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Pendientes", "Leídos").forEach { opcion ->
                    FilterChip(selected = filtro == opcion, onClick = { filtro = opcion },
                        label = { Text(opcion) })
                }
            }
            if (filtrados.isEmpty()) Text(
                if (filtro == "Todos") "Aún no tienes avisos. Las novedades aparecerán aquí."
                else "No hay avisos en esta categoría.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtrados, key = { it.id }) { aviso ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(if (aviso.leido) "Notificación leída" else "Novedad pendiente",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (aviso.leido) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.primary)
                            Text(aviso.numeroSiniestro, style = MaterialTheme.typography.titleMedium)
                            Text(aviso.mensaje)
                            Text(aviso.fecha, style = MaterialTheme.typography.bodySmall)
                            val requiereDocumento = aviso.mensaje.startsWith(
                                "Necesitamos el documento", ignoreCase = true
                            ) || (aviso.mensaje.startsWith("Recordatorio #") &&
                                aviso.mensaje.contains("entrega pendiente", ignoreCase = true))
                            Button(onClick = {
                                onMarcarLeido(aviso.id)
                                if (requiereDocumento) onAdjuntarDocumento(aviso.siniestroId)
                                else onAbrirCaso(aviso.siniestroId)
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (requiereDocumento) "Adjuntar ahora" else "Ver siniestro")
                            }
                            if (!aviso.leido) {
                                TextButton(onClick = { onMarcarLeido(aviso.id) }) {
                                    Text("Marcar como leído")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
