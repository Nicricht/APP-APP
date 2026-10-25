package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) { onCargar() }
    Scaffold(topBar = { TopAppBar(title = { Text("Avisos de mis siniestros") }) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver a mis siniestros") }
            Text(
                "Avisos guardados en este teléfono. No son notificaciones enviadas por una aseguradora.",
                style = MaterialTheme.typography.bodySmall
            )
            if (avisos.isEmpty()) Text("No tienes avisos nuevos ni anteriores.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(avisos, key = { it.id }) { aviso ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(aviso.numeroSiniestro, style = MaterialTheme.typography.titleMedium)
                            Text(aviso.mensaje)
                            Text(aviso.fecha, style = MaterialTheme.typography.bodySmall)
                            Text(if (aviso.leido) "Leído" else "No leído")
                            TextButton(onClick = {
                                onMarcarLeido(aviso.id)
                                onAbrirCaso(aviso.siniestroId)
                            }) { Text("Ver siniestro") }
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
