package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarSiniestroScreen(
    siniestro: Siniestro?,
    onVolver: () -> Unit,
    onGuardar: (String) -> Unit
) {
    var descripcion by remember(siniestro?.id) { mutableStateOf(siniestro?.descripcion.orEmpty()) }
    val error = descripcion.isBlank()
    Scaffold(topBar = { BarraSuperior("Corregir reporte") }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (siniestro == null || siniestro.etapa != EtapaSiniestro.RECIBIDO) {
                Text("Este reporte no se puede editar")
            } else {
                CodigoSiniestro(siniestro.numero)
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error,
                    supportingText = { if (error) Text("La descripción es obligatoria") },
                    minLines = 3
                )
                Button(onClick = { onGuardar(descripcion) }, enabled = !error) {
                    Text("Guardar cambios")
                }
            }
        }
    }
}
