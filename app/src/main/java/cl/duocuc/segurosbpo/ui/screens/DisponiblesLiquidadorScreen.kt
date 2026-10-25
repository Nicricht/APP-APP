package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponiblesLiquidadorScreen(
    liquidador: Liquidador,
    disponibles: List<Siniestro>,
    onTomarCaso: (Long) -> String?
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { BarraSuperior("Casos disponibles") },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Sin asignar en " + liquidador.equipo,
                style = MaterialTheme.typography.titleLarge
            )
            if (disponibles.isEmpty()) {
                Text("No hay siniestros pendientes de asignación en tu área.")
            }
            disponibles.forEach { disponible ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CodigoSiniestro(disponible.numero)
                        Text(disponible.tipo.etiqueta)
                        Text("Recibido · Sin liquidador asignado")
                        Button(onClick = {
                            val error = onTomarCaso(disponible.id)
                            scope.launch {
                                aviso.showSnackbar(
                                    error ?: "Caso asignado. Ya puedes gestionarlo."
                                )
                            }
                        }) { Text("Tomar caso") }
                    }
                }
            }
        }
    }
}
