package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.AvatarCircular
import cl.duocuc.segurosbpo.ui.components.EtiquetaInteres
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.ValidadorSiniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSiniestrosScreen(
    asegurado: Asegurado,
    siniestros: List<Siniestro>,
    busqueda: String,
    avisosNoLeidos: Int,
    onVerAvisos: () -> Unit,
    onBuscar: (String) -> Unit,
    onCambiarAsegurado: (Asegurado) -> Unit,
    onReportar: () -> Unit,
    onGestionar: () -> Unit,
    onVerDetalle: (Long) -> Unit,
    onVerEstadisticas: () -> Unit = {},
    permitirCambioAsegurado: Boolean = true,
    permitirGestor: Boolean = true,
    onCerrarSesion: () -> Unit = {},
    onVerPerfil: () -> Unit = {}
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarCircular(
                    imagen = R.drawable.avatar_usuario,
                    descripcion = "Avatar ilustrativo del asegurado",
                    tamano = 56.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Asegurado: ${asegurado.etiqueta}")
                    Text("RUT: ${asegurado.rutOculto}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (permitirCambioAsegurado) {
                    TextButton(onClick = { mostrarUsuarios = true }) { Text("Cambiar asegurado") }
                } else {
                    TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                }
                Button(onClick = onReportar) { Text("Reportar") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (permitirGestor) TextButton(onClick = onGestionar) { Text("Liquidador") }
                TextButton(onClick = onVerPerfil) { Text("Mi perfil") }
                TextButton(onClick = onVerAvisos) {
                    Text(if (avisosNoLeidos > 0) "Avisos ($avisosNoLeidos)" else "Avisos")
                }
            }
            Button(onClick = onVerEstadisticas, modifier = Modifier.fillMaxWidth()) {
                Text("Mis estadísticas")
            }
            Text(
                "Demostración académica. Los registros y documentos se guardan en este teléfono.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBuscar,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar siniestro") },
                placeholder = { Text("Número, compañía, tipo o comuna") },
                singleLine = true
            )
            AnimatedVisibility(
                visible = siniestros.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text(if (busqueda.isBlank()) "Todavía no tienes siniestros registrados" else "No hay siniestros con ese criterio")
                        Text(if (busqueda.isBlank()) "Puedes reportar uno para comenzar su seguimiento." else "Prueba con otro número, compañía o tipo.")
                    }
                }
            }
            if (siniestros.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(siniestros, key = { it.id }) { siniestro ->
                        Card(onClick = { onVerDetalle(siniestro.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(siniestro.numero, style = MaterialTheme.typography.titleMedium)
                                Text(siniestro.compania.etiqueta)
                                Text(siniestro.tipo.etiqueta)
                                EtiquetaInteres(texto = "Estado: ${siniestro.etapa.etiqueta}")
                                if (ValidadorSiniestro.reporteFueraDePlazo(
                                        siniestro.fechaOcurrencia, siniestro.fechaReporte
                                    )) {
                                    Text(
                                        "Reporte fuera de plazo",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text("Actualizado: ${siniestro.ultimaActualizacion}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
