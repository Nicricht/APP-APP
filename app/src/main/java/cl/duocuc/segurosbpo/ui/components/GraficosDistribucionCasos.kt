package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EstadisticasCasos
import cl.duocuc.segurosbpo.model.PuntoEstadistico
import cl.duocuc.segurosbpo.model.Siniestro

@Composable
private fun GraficoFilasEstadisticas(puntos: List<PuntoEstadistico>) {
    val maximo = maxOf(1, puntos.maxOfOrNull { it.cantidad } ?: 1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        puntos.forEach { punto ->
            Text(
                punto.etiqueta + ": " + punto.cantidad,
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = { punto.cantidad.toFloat() / maximo },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GraficosDistribucionCasos(casos: List<Siniestro>) {
    val puntos = remember(casos) { EstadisticasCasos.porTipo(casos) }
    GraficoFilasEstadisticas(puntos)
}

@Composable
fun GraficoEvolucionCasos(casos: List<Siniestro>) {
    val puntos = remember(casos) { EstadisticasCasos.porMes(casos) }
    GraficoFilasEstadisticas(puntos)
}
