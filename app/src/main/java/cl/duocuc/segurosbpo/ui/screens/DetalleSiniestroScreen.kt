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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.ValidadorSiniestro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleSiniestroScreen(
    siniestro: Siniestro?,
    solicitudes: List<SolicitudDocumento>,
    onCargarSolicitudes: () -> Unit,
    onVolver: () -> Unit,
    onEditar: () -> Unit,
    onEvidencias: () -> Unit,
    onHistorial: () -> Unit
) {
    LaunchedEffect(siniestro?.id) {
        if (siniestro != null) onCargarSolicitudes()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del siniestro") },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                }
            )
        }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (siniestro == null) {
                Text("No se encontró el siniestro de este asegurado")
            } else {
                Text(siniestro.numero, style = MaterialTheme.typography.headlineSmall)
                val pendientes = solicitudes.filter { it.pendiente }
                val puedeAdjuntar = siniestro.etapa != EtapaSiniestro.CERRADO
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Estado actual", style = MaterialTheme.typography.labelLarge)
                        Text(siniestro.etapa.etiqueta, style = MaterialTheme.typography.headlineSmall)
                        Text("Última actualización: ${siniestro.ultimaActualizacion}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Próximo paso", style = MaterialTheme.typography.titleLarge)
                val proximoPaso = if (pendientes.isNotEmpty() && !puedeAdjuntar)
                    "Este caso está cerrado. Puedes revisar los documentos, pero ya no admite nuevos adjuntos."
                else if (pendientes.isNotEmpty())
                    "Adjunta los documentos solicitados para que tu caso pueda continuar."
                else when (siniestro.etapa) {
                    EtapaSiniestro.RECIBIDO -> "Estamos revisando tu reporte para asignarlo a un liquidador. No necesitas hacer nada por ahora."
                    EtapaSiniestro.EVALUACION -> "El liquidador está revisando tu caso. Te avisaremos si necesitamos documentos."
                    EtapaSiniestro.LIQUIDACION -> "Estamos definiendo la resolución con tu compañía de seguros."
                    EtapaSiniestro.CERRADO -> "Tu caso terminó. Revisa el resultado registrado."
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(proximoPaso)
                        pendientes.forEach { solicitud ->
                            Text("• ${solicitud.tipo}", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error)
                        }
                        Button(onClick = onEvidencias, modifier = Modifier.fillMaxWidth()) {
                            Text(if (pendientes.isEmpty() || !puedeAdjuntar) "Ver documentos y evidencias"
                                else "Adjuntar documento")
                        }
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Datos del siniestro", style = MaterialTheme.typography.titleMedium)
                        Text("Compañía: ${siniestro.compania.etiqueta}")
                        Text("Tipo: ${siniestro.tipo.etiqueta}")
                        Text("Fecha: ${siniestro.fechaOcurrencia}")
                        Text("Comuna: ${siniestro.comuna}")
                        Text("Descripción: ${siniestro.descripcion}")
                        Text("Liquidador: " + (siniestro.liquidadorAsignado?.nombre
                            ?: "Pendiente de asignación"))
                        if (ValidadorSiniestro.reporteFueraDePlazo(
                                siniestro.fechaOcurrencia, siniestro.fechaReporte)) {
                            Text("Reporte fuera de plazo (más de 30 días desde el siniestro)",
                                color = MaterialTheme.colorScheme.error)
                        }
                        if (siniestro.etapa == EtapaSiniestro.CERRADO) {
                            Text("Resultado: " + (siniestro.resultadoCierre ?: "No informado"))
                            Text("Mensaje de cierre: " + (siniestro.mensajeCierre ?: "No disponible"))
                        }
                    }
                }
                OutlinedButton(onClick = onHistorial, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver historial de gestiones")
                }
                if (siniestro.etapa == EtapaSiniestro.RECIBIDO) {
                    OutlinedButton(onClick = onEditar, modifier = Modifier.fillMaxWidth()) {
                        Text("Corregir descripción")
                    }
                }
            }
        }
    }
}
