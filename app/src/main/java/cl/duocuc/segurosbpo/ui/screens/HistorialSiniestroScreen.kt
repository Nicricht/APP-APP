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
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TextoPresentacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSiniestroScreen(
    siniestro: Siniestro?,
    gestiones: List<Gestion>,
    onCargar: () -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(siniestro?.id) { if (siniestro != null) onCargar() }
    Scaffold(topBar = { TopAppBar(title = { Text("Historial del siniestro") }) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver al detalle") }
            if (siniestro == null) {
                Text("No se encontró un siniestro de este asegurado")
            } else {
                Text(siniestro.numero, style = MaterialTheme.typography.titleLarge)
                Text("Gestiones visibles para el asegurado")
                if (gestiones.isEmpty()) Text("Todavía no existen movimientos registrados.")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(gestiones, key = { it.id }) { gestion ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(gestion.fecha, style = MaterialTheme.typography.labelMedium)
                                Text(TextoPresentacion.gestion(gestion.descripcion), style = MaterialTheme.typography.bodyLarge)
                                Text("Responsable: "+TextoPresentacion.responsable(gestion.responsable),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
