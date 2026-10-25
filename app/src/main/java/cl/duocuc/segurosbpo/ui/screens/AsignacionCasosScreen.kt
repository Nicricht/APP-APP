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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignacionCasosScreen(
    pendientes: List<Siniestro>,
    carga: Map<Liquidador, Int>,
    actualizadoEn: String,
    onCargar: () -> Unit,
    onAsignar: (Long, Liquidador) -> String?
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { onCargar() }

    Scaffold(
        topBar = { BarraSuperior("Casos por asignar") },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Casos sin liquidador: " + pendientes.size,
                style = MaterialTheme.typography.titleLarge
            )
            Text("Datos al " + actualizadoEn, style = MaterialTheme.typography.bodySmall)

            if (pendientes.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Todos los casos recibidos ya tienen equipo asignado.")
                    }
                }
            }

            pendientes.forEach { caso ->
                val equipo = Liquidador.entries.first { it.esVehiculo == caso.tipo.esVehiculo }
                val espera = ControlPlazos.diasHabilesTranscurridos(caso.fechaReporte)
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CodigoSiniestro(caso.numero)
                        Text(caso.tipo.etiqueta)
                        Text(caso.compania.etiqueta, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Asegurado: " + caso.asegurado.etiqueta,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Reportado: " + caso.fechaReporte + " · " + espera +
                                " día(s) hábil(es) en espera",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (espera >= 2) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Equipo " + equipo.equipo + " · " +
                                    (carga[equipo] ?: 0) + " caso(s) activos",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                val error = onAsignar(caso.id, equipo)
                                scope.launch {
                                    aviso.showSnackbar(
                                        error ?: ("Caso asignado al equipo " + equipo.equipo)
                                    )
                                }
                            }) { Text("Asignar") }
                        }
                    }
                }
            }
        }
    }
}
