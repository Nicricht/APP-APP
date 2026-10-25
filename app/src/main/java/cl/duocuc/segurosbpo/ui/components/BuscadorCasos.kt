package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro

@Composable
fun BuscadorCasos(texto: String, onTexto: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTexto,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Buscar caso") },
        placeholder = { Text("Número, tipo, comuna o asegurado") },
        singleLine = true,
        trailingIcon = {
            if (texto.isNotEmpty()) {
                TextButton(onClick = { onTexto("") }) { Text("Limpiar") }
            }
        }
    )
}

@Composable
fun FiltroEtapas(
    etapas: List<EtapaSiniestro>,
    seleccionada: EtapaSiniestro?,
    onSeleccionar: (EtapaSiniestro?) -> Unit,
    extra: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = seleccionada == null,
            onClick = { onSeleccionar(null) },
            label = { Text("Todos") }
        )
        etapas.forEach { etapa ->
            FilterChip(
                selected = seleccionada == etapa,
                onClick = { onSeleccionar(if (seleccionada == etapa) null else etapa) },
                label = { Text(etapa.etiqueta) }
            )
        }
        extra()
    }
}
