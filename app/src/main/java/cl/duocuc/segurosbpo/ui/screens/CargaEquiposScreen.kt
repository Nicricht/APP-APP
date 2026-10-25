package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.ui.components.BarraSuperior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaEquiposScreen(
    carga: Map<Liquidador, Int>,
    pendientes: List<Siniestro>,
    actualizadoEn: String,
    onCargar: () -> Unit
) {
    LaunchedEffect(Unit) { onCargar() }

    val total = carga.values.sum()
    val mayor = carga.values.maxOrNull() ?: 0

    Scaffold(topBar = { BarraSuperior("Carga de los equipos") }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Resumen de asignaciones", style = MaterialTheme.typography.titleLarge)
            Text("Datos al " + actualizadoEn, style = MaterialTheme.typography.bodySmall)

            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Linea("Casos activos con equipo", total.toString())
                    Linea("Casos esperando asignación", pendientes.size.toString())
                }
            }

            Liquidador.entries.forEach { equipo ->
                val cantidad = carga[equipo] ?: 0
                val enEspera = pendientes.count { it.tipo.esVehiculo == equipo.esVehiculo }
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Equipo " + equipo.equipo,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(equipo.nombre, style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(
                            progress = { if (mayor == 0) 0f else cantidad.toFloat() / mayor },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Linea("Casos activos", cantidad.toString())
                        Linea("Esperando asignación", enEspera.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun Linea(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
