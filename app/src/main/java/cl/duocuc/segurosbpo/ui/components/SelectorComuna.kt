package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.ComunasSugeridas

@Composable
fun SelectorComuna(
    comuna: String,
    error: String?,
    onSeleccionar: (String) -> Unit
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { busqueda = comuna; abierto = true }
        ) { Text(if (comuna.isBlank()) "Seleccionar comuna" else "Comuna: $comuna") }
        if (error != null) Text(error, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
    }
    if (abierto) {
        AlertDialog(
            onDismissRequest = { abierto = false },
            title = { Text("¿Dónde ocurrió el siniestro?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        label = { Text("Buscar comuna") },
                        placeholder = { Text("Escribe para filtrar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    val resultados = ComunasSugeridas.buscar(busqueda)
                    if (resultados.isEmpty()) {
                        Text("No aparece en la lista. Puedes usar el nombre que escribiste.")
                    }
                    LazyColumn(Modifier.heightIn(max = 280.dp)) {
                        items(resultados) { opcion ->
                            Text(
                                opcion,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onSeleccionar(opcion)
                                    abierto = false
                                }.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = busqueda.isNotBlank(), onClick = {
                    onSeleccionar(busqueda.trim())
                    abierto = false
                }) { Text("Usar esta comuna") }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) { Text("Cancelar") }
            }
        )
    }
}
