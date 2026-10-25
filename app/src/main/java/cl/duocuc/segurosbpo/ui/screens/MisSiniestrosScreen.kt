package cl.duocuc.segurosbpo.ui.screens

import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.AvatarCuenta
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import cl.duocuc.segurosbpo.ui.components.EtiquetaInteres
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.model.ValidadorSiniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSiniestrosScreen(
    asegurado: Asegurado,
    fotoCuenta: ByteArray?,
    actualizadoEn: String,
    siniestros: List<Siniestro>,
    busqueda: String,
    onBuscar: (String) -> Unit,
    onVerDetalle: (Long) -> Unit,
    onNuevoReporte: () -> Unit
) {
    Scaffold(
        topBar = { BarraSuperior("Mis siniestros") },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoReporte) {
                Icon(
                    painter = painterResource(R.drawable.ic_mas),
                    contentDescription = "Reportar un siniestro nuevo"
                )
            }
        }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Seguimiento de seguros", style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarCuenta(
                    foto = fotoCuenta,
                    descripcion = "Foto de la persona asegurada",
                    tamano = 56.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Asegurado: ${asegurado.etiqueta}")
                    Text(
                        "RUT: " + ValidadorRut.ocultar(asegurado.rut),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Datos al " + actualizadoEn,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
                                CodigoSiniestro(siniestro.numero)
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
