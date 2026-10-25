package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasLiquidadorScreen(
    liquidador: Liquidador,
    casos: List<Siniestro>,
    disponibles: List<Siniestro>,
    seguimientosVencidos: Set<Long>
) {
    val cerrados = casos.count { it.etapa == EtapaSiniestro.CERRADO }
    val activos = casos.size - cerrados

    Scaffold(topBar = { BarraSuperior("Estadísticas") }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Equipo de " + liquidador.equipo, style = MaterialTheme.typography.titleLarge)
            Text("Responsable: " + liquidador.nombre)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Indicador("Casos asignados", casos.size)
                    Indicador("En gestión", activos)
                    Indicador("Cerrados", cerrados)
                    Indicador("Disponibles por tomar", disponibles.size)
                    Indicador("Seguimientos vencidos", seguimientosVencidos.size)
                }
            }

            ResumenGraficosCasos(casos = casos, equipo = liquidador.equipo)
        }
    }
}

@Composable
private fun Indicador(etiqueta: String, valor: Int) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor.toString(), style = MaterialTheme.typography.headlineSmall)
    }
}
