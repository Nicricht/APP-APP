package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Consulta
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.ValidadorConsulta
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultasScreen(
    siniestro: Siniestro?,
    consultas: List<Consulta>,
    onCargar: () -> Unit,
    onEnviar: (String) -> String?,
    onVolver: () -> Unit
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pregunta by rememberSaveable(siniestro?.id) { mutableStateOf("") }
    var tocado by rememberSaveable(siniestro?.id) { mutableStateOf(false) }

    LaunchedEffect(siniestro?.id) { onCargar() }

    val error = ValidadorConsulta.errorPregunta(pregunta)

    Scaffold(
        topBar = { BarraSuperior("Consultas del caso", onVolver = onVolver) }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (siniestro == null) {
                Text("Este siniestro no pertenece al asegurado seleccionado")
                return@Column
            }

            CodigoSiniestro(siniestro.numero, estilo = MaterialTheme.typography.titleLarge)

            if (siniestro.etapa == EtapaSiniestro.CERRADO) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("El caso está cerrado y ya no admite nuevas consultas.")
                    }
                }
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Nueva consulta", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = pregunta,
                            onValueChange = { pregunta = it; tocado = true },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Escribe tu consulta") },
                            minLines = 3,
                            isError = tocado && error != null,
                            supportingText = {
                                Text(
                                    if (tocado && error != null) error
                                    else pregunta.length.toString() + "/" +
                                        ValidadorConsulta.MAX_PREGUNTA
                                )
                            }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = error == null,
                            onClick = {
                                val resultado = onEnviar(pregunta)
                                scope.launch {
                                    aviso.showSnackbar(resultado ?: "Consulta enviada")
                                }
                                if (resultado == null) {
                                    pregunta = ""
                                    tocado = false
                                }
                            }
                        ) { Text("Enviar consulta") }
                    }
                }
            }

            Text(
                "Consultas realizadas (" + consultas.size + ")",
                style = MaterialTheme.typography.titleMedium
            )
            if (consultas.isEmpty()) {
                Text("Todavía no has enviado consultas sobre este caso.")
            }
            consultas.forEach { consulta ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(consulta.pregunta, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Enviada: " + consulta.fechaPregunta,
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider()
                        if (consulta.respondida) {
                            Text("Respuesta", style = MaterialTheme.typography.titleSmall)
                            Text(consulta.respuesta.orEmpty())
                            Text(
                                "Respondida: " + consulta.fechaRespuesta.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                "En espera de respuesta del equipo liquidador",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        AvisoSuperior(aviso)
        }
    }
}
