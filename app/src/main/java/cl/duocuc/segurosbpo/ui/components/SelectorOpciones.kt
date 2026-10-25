package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> SelectorOpciones(
    titulo: String,
    opciones: List<T>,
    seleccionada: T?,
    onSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar una opción"
) {
    var mostrar by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedButton(
            onClick = { mostrar = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = seleccionada?.let(textoDe) ?: placeholder,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                color = if (seleccionada == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
            )
            Text(text = if (seleccionada == null) "Elegir" else "Cambiar",
                color = MaterialTheme.colorScheme.primary)
        }
    }

    if (mostrar) {
        AlertDialog(
            onDismissRequest = { mostrar = false },
            title = { Text(titulo) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(opciones) { opcion ->
                        TextButton(
                            onClick = {
                                onSeleccionar(opcion)
                                mostrar = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (if (opcion == seleccionada) "✓  " else "") + textoDe(opcion),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrar = false }) { Text("Cancelar") }
            }
        )
    }
}
