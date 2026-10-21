package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Siniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSiniestrosScreen(
    asegurado: Asegurado,
    siniestros: List<Siniestro>,
    busqueda: String,
    onBuscar: (String) -> Unit,
    onCambiarAsegurado: (Asegurado) -> Unit,
    onReportar: () -> Unit,
    onVerDetalle: (Long) -> Unit
) {
    var mostrarUsuarios by remember { mutableStateOf(false) }
    if (mostrarUsuarios) {
        AlertDialog(
            onDismissRequest = { mostrarUsuarios = false },
            title = { Text("Selecciona un asegurado ficticio") },
            text = {
                LazyColumn {
                    items(Asegurado.entries) { usuario ->
                        Text(
                            usuario.etiqueta,
                            Modifier.fillMaxWidth().clickable {
                                onCambiarAsegurado(usuario)
                                mostrarUsuarios = false
                            }.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarUsuarios = false }) { Text("Cerrar") } }
        )
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Mis siniestros") }) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Seguimiento de seguros", style = MaterialTheme.typography.titleLarge)
            Text("Asegurado: ${asegurado.etiqueta}")
            Text("RUT: ${asegurado.rutOculto}", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { mostrarUsuarios = true }) { Text("Cambiar asegurado") }
                Button(onClick = onReportar) { Text("Reportar") }
            }
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBuscar,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar siniestro") },
                placeholder = { Text("Número, compañía, tipo o comuna") },
                singleLine = true
            )
            if (siniestros.isEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text(if (busqueda.isBlank()) "Todavía no tienes siniestros registrados" else "No hay siniestros con ese criterio")
                        Text(if (busqueda.isBlank()) "Puedes reportar uno para comenzar su seguimiento." else "Prueba con otro número, compañía o tipo.")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(siniestros, key = { it.id }) { siniestro ->
                        Card(onClick = { onVerDetalle(siniestro.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(siniestro.numero, style = MaterialTheme.typography.titleMedium)
                                Text(siniestro.compania.etiqueta)
                                Text(siniestro.tipo.etiqueta)
                                Text("Estado: ${siniestro.etapa.etiqueta}", color = MaterialTheme.colorScheme.primary)
                                Text("Actualizado: ${siniestro.ultimaActualizacion}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
