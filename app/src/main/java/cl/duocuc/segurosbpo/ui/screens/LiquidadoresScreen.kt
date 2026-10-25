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
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.ValidadorContacto
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.AvatarCuenta
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidadoresScreen(
    liquidadores: List<CuentaLocal>,
    carga: Map<String, Int>,
    onCrear: (String, Liquidador, String) -> String?
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var nombre by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var equipo by rememberSaveable { mutableStateOf(Liquidador.VEHICULOS.name) }
    var tocado by rememberSaveable { mutableStateOf(false) }

    val equipoElegido = Liquidador.entries.first { it.name == equipo }
    val errorNombre = when {
        nombre.isBlank() -> "Indica el nombre"
        nombre.trim().length < 3 -> "El nombre es muy corto"
        !nombre.trim().contains(' ') -> "Escribe nombre y apellido"
        else -> null
    }
    val errorTelefono = ValidadorContacto.errorTelefono(telefono)
    val puedeCrear = errorNombre == null && errorTelefono == null

    Scaffold(topBar = { BarraSuperior("Equipo de liquidadores") }) { margen ->
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
                            "Agregar un liquidador",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it; tocado = true },
                            label = { Text("Nombre y apellido") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = tocado && errorNombre != null,
                            supportingText = {
                                Text(
                                    if (tocado && errorNombre != null) errorNombre
                                    else "Se le creará un correo corporativo automáticamente"
                                )
                            }
                        )
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it; tocado = true },
                            label = { Text("Teléfono de contacto") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = tocado && errorTelefono != null,
                            supportingText = {
                                Text(
                                    if (tocado && errorTelefono != null) errorTelefono
                                    else "Solo números, entre 8 y 11 dígitos"
                                )
                            }
                        )
                        Text("Área", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Liquidador.entries.forEach { area ->
                                FilterChip(
                                    selected = equipo == area.name,
                                    onClick = { equipo = area.name },
                                    label = { Text(area.equipo) }
                                )
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = puedeCrear,
                            onClick = {
                                val error = onCrear(nombre, equipoElegido, telefono)
                                scope.launch {
                                    aviso.showSnackbar(error ?: "Liquidador agregado")
                                }
                                if (error == null) {
                                    nombre = ""
                                    telefono = ""
                                    tocado = false
                                }
                            }
                        ) { Text("Agregar al equipo") }
                    }
                }

                Liquidador.entries.forEach { area ->
                    val delArea = liquidadores.filter { it.equipo == area.name }
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Equipo " + area.equipo + " (" + delArea.size + ")",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (delArea.isEmpty()) {
                                Text(
                                    "Esta área no tiene liquidadores.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            delArea.forEachIndexed { indice, cuenta ->
                                if (indice > 0) HorizontalDivider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AvatarCuenta(
                                        foto = cuenta.foto,
                                        descripcion = cuenta.nombre,
                                        tamano = 44.dp
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            cuenta.nombre,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            cuenta.correo,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        (carga[cuenta.id] ?: 0).toString() + " caso(s)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            AvisoSuperior(aviso)
        }
    }
}
