package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.coincideCon
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BuscadorCasos
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignacionCasosScreen(
    pendientes: List<Siniestro>,
    carga: Map<Liquidador, Int>,
    equipos: Map<Liquidador, List<CuentaLocal>>,
    cargaPersonas: Map<String, Int>,
    actualizadoEn: String,
    onCargar: () -> Unit,
    onAsignar: (Long, Liquidador, String, String) -> String?
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var busqueda by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) { onCargar() }

    Scaffold(
        topBar = { BarraSuperior("Casos por asignar") }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Casos sin liquidador: " + pendientes.size,
                style = MaterialTheme.typography.titleLarge
            )
            Text("Datos al " + actualizadoEn, style = MaterialTheme.typography.bodySmall)

            if (pendientes.isNotEmpty()) {
                BuscadorCasos(texto = busqueda, onTexto = { busqueda = it })
            }

            val visibles = pendientes.filter { it.coincideCon(busqueda) }

            if (pendientes.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Todos los casos recibidos ya tienen equipo asignado.")
                    }
                }
            } else if (visibles.isEmpty()) {
                Text("Ningún caso coincide con la búsqueda.")
            }

            visibles.forEach { caso ->
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
                        Text(
                            "Equipo " + equipo.equipo + " · " +
                                (carga[equipo] ?: 0) + " caso(s) activos",
                            style = MaterialTheme.typography.bodySmall
                        )

                        val candidatos = equipos[equipo].orEmpty()
                        if (candidatos.isEmpty()) {
                            Text(
                                "El equipo " + equipo.equipo + " no tiene liquidadores. " +
                                    "Agrega uno antes de asignar este caso.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "Asignar a:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            candidatos.forEach { persona ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            persona.nombre,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            (cargaPersonas[persona.id] ?: 0).toString() +
                                                " caso(s) activos",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Button(onClick = {
                                        val error = onAsignar(
                                            caso.id, equipo, persona.id, persona.nombre
                                        )
                                        scope.launch {
                                            aviso.showSnackbar(
                                                error ?: ("Caso asignado a " + persona.nombre)
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
        AvisoSuperior(aviso)
        }
    }
}
