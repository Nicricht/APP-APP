package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.ValidadorSiniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSiniestroScreen(siniestro: Siniestro?, onVolver: () -> Unit, onEditar: () -> Unit, onEvidencias: () -> Unit, onHistorial: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle del siniestro") }) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver") }
            if (siniestro == null) {
                Text("No se encontró el siniestro de este asegurado")
            } else {
                Text(siniestro.numero, style = MaterialTheme.typography.headlineSmall)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Etapa: ${siniestro.etapa.etiqueta}", style = MaterialTheme.typography.titleLarge)
                        Text("Compañía: ${siniestro.compania.etiqueta}")
                        Text("Tipo: ${siniestro.tipo.etiqueta}")
                        Text("Ocurrencia: ${siniestro.fechaOcurrencia}")
                        if (ValidadorSiniestro.reporteFueraDePlazo(
                                siniestro.fechaOcurrencia, siniestro.fechaReporte
                            )) {
                            Text(
                                "Reporte fuera de plazo (más de 30 días desde el siniestro)",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text("Reporte: ${siniestro.fechaReporte}")
                        Text("Comuna: ${siniestro.comuna}")
                        Text("Descripción: ${siniestro.descripcion}")
                        Text("Última actualización: ${siniestro.ultimaActualizacion}")
                        Text("Liquidador: " + (siniestro.liquidadorAsignado?.nombre ?: "Pendiente de asignación"))
                        if (siniestro.etapa == EtapaSiniestro.CERRADO) {
                            Text("Resultado: " + (siniestro.resultadoCierre ?: "No informado"))
                            Text("Mensaje de cierre: " + (siniestro.mensajeCierre ?: "No disponible"))
                        }
                    }
                }
                Text("¿Qué sigue?", style = MaterialTheme.typography.titleMedium)
                val proximoPaso = when (siniestro.etapa) {
                    EtapaSiniestro.RECIBIDO -> "Estamos revisando tu reporte para asignarlo a un liquidador. No necesitas hacer nada por ahora."
                    EtapaSiniestro.EVALUACION -> "El liquidador está revisando tu caso. Te avisaremos si necesitamos documentos."
                    EtapaSiniestro.LIQUIDACION -> "Estamos definiendo la resolución con tu compañía de seguros."
                    EtapaSiniestro.CERRADO -> "Tu caso terminó. Revisa el resultado registrado."
                }
                Text(proximoPaso)
                Button(onClick = onHistorial) { Text("Ver historial") }
                Button(onClick = onEvidencias) { Text("Documentos y evidencias") }
                if (siniestro.etapa == EtapaSiniestro.RECIBIDO) {
                    Button(onClick = onEditar) { Text("Corregir descripción") }
                }
            }
        }
    }
}
