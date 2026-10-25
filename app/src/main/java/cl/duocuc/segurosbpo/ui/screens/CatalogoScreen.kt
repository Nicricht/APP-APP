package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    companias: List<Compania>,
    tipos: List<TipoSiniestro>,
    onCrearCompania: (String, Int) -> String?,
    onCrearTipo: (String, Boolean, List<String>) -> String?
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var nombreCompania by rememberSaveable { mutableStateOf("") }
    var plazo by rememberSaveable { mutableStateOf("10") }
    var nombreTipo by rememberSaveable { mutableStateOf("") }
    var esVehiculo by rememberSaveable { mutableStateOf(true) }
    var documentos by rememberSaveable { mutableStateOf("") }

    val plazoValido = plazo.toIntOrNull()?.let { it in 1..60 } == true

    Scaffold(topBar = { BarraSuperior("Catálogos del sistema") }) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Agregar compañía de seguros",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = nombreCompania,
                            onValueChange = { nombreCompania = it },
                            label = { Text("Nombre de la compañía") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = plazo,
                            onValueChange = { plazo = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Plazo de respuesta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = plazo.isNotEmpty() && !plazoValido,
                            supportingText = { Text("Días hábiles comprometidos, de 1 a 60") }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nombreCompania.trim().length >= 3 && plazoValido,
                            onClick = {
                                val error = onCrearCompania(
                                    nombreCompania, plazo.toIntOrNull() ?: 10
                                )
                                scope.launch {
                                    aviso.showSnackbar(error ?: "Compañía agregada")
                                }
                                if (error == null) {
                                    nombreCompania = ""
                                    plazo = "10"
                                }
                            }
                        ) { Text("Agregar compañía") }
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Compañías registradas (" + companias.size + ")",
                            style = MaterialTheme.typography.titleMedium
                        )
                        companias.forEachIndexed { indice, compania ->
                            if (indice > 0) HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        compania.etiqueta,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (!compania.activa) {
                                        Text(
                                            "No disponible",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Text(
                                    compania.plazoDiasHabiles.toString() + " días",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Agregar tipo de siniestro",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = nombreTipo,
                            onValueChange = { nombreTipo = it },
                            label = { Text("Nombre del tipo") },
                            placeholder = { Text("Ej.: Hogar · rotura de cañería") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text("Área responsable", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = esVehiculo,
                                onClick = { esVehiculo = true },
                                label = { Text("Vehículos") }
                            )
                            FilterChip(
                                selected = !esVehiculo,
                                onClick = { esVehiculo = false },
                                label = { Text("Hogar") }
                            )
                        }
                        OutlinedTextField(
                            value = documentos,
                            onValueChange = { documentos = it },
                            label = { Text("Documentos habituales") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            supportingText = { Text("Uno por línea") }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nombreTipo.trim().length >= 3,
                            onClick = {
                                val error = onCrearTipo(
                                    nombreTipo,
                                    esVehiculo,
                                    documentos.split("\n")
                                )
                                scope.launch {
                                    aviso.showSnackbar(error ?: "Tipo de siniestro agregado")
                                }
                                if (error == null) {
                                    nombreTipo = ""
                                    documentos = ""
                                }
                            }
                        ) { Text("Agregar tipo") }
                    }
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Tipos registrados (" + tipos.size + ")",
                            style = MaterialTheme.typography.titleMedium
                        )
                        tipos.forEachIndexed { indice, tipo ->
                            if (indice > 0) HorizontalDivider()
                            Text(tipo.etiqueta, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (tipo.esVehiculo) "Equipo Vehículos" else "Equipo Hogar",
                                style = MaterialTheme.typography.bodySmall
                            )
                            tipo.documentosHabituales.forEach { documento ->
                                Text(
                                    "· " + documento,
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
