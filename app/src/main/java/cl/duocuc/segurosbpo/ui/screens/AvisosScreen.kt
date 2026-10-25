package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.AvisoSiniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisosScreen(
    avisos: List<AvisoSiniestro>,
    onCargar: () -> Unit,
    onMarcarLeido: (Long) -> Unit,
    onAbrirCaso: (Long) -> Unit,
    onAdjuntar: (Long) -> Unit,
    onVolver: () -> Unit
) {
    LaunchedEffect(Unit) { onCargar() }
    Scaffold(topBar = { BarraSuperior("Avisos de mis siniestros") }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (avisos.isEmpty()) Text("No tienes avisos nuevos ni anteriores.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(avisos, key = { it.id }) { aviso ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CodigoSiniestro(aviso.numeroSiniestro)
                            Text(aviso.mensaje)
                            Text(
                                aviso.fecha + " · " + (if (aviso.leido) "Leído" else "No leído"),
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (aviso.documentoPedido != null) {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        onMarcarLeido(aviso.id)
                                        onAdjuntar(aviso.siniestroId)
                                    }
                                ) { Text("Enviar " + aviso.documentoPedido) }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = {
                                    onMarcarLeido(aviso.id)
                                    onAbrirCaso(aviso.siniestroId)
                                }) { Text("Ver siniestro") }
                                if (!aviso.leido) {
                                    TextButton(onClick = { onMarcarLeido(aviso.id) }) {
                                        Text("Marcar como leído")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
