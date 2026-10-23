package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasAseguradoScreen(
    siniestros: List<Siniestro>,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis estadísticas") }) }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver a mis siniestros") }
            ResumenGraficosCasos(
                casos = siniestros,
                equipo = "",
                titulo = "Estadísticas de mis siniestros",
                descripcion = "Siniestros registrados"
            )
        }
    }
}
