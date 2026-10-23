package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.Liquidador

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccesoScreen(
    onIniciar: (String, String) -> String?,
    onRegistrar: (String, String, String, String, String, String) -> String?,
    onDemoLiquidador: (Liquidador) -> Unit
) {
    var registro by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Seguros BPO") }) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (registro) "Crear cuenta de asegurado" else "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Tus siniestros y documentos se almacenan en este teléfono.",
                style = MaterialTheme.typography.bodySmall
            )
            if (registro) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rut, onValueChange = { rut = it },
                    label = { Text("RUT ficticio") }, modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = correo, onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (registro) {
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            OutlinedTextField(
                value = clave, onValueChange = { clave = it },
                label = { Text("Contraseña de demostración") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (registro) {
                OutlinedTextField(
                    value = confirmacion, onValueChange = { confirmacion = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    mensaje = if (registro) {
                        onRegistrar(nombre, rut, correo, telefono, clave, confirmacion)
                    } else onIniciar(correo, clave)
                }
            ) {
                Text(if (registro) "Crear cuenta" else "Iniciar sesión")
            }
            TextButton(
                onClick = {
                    registro = !registro
                    mensaje = null
                    clave = ""
                    confirmacion = ""
                }
            ) {
                Text(if (registro) "Ya tengo cuenta" else "Crear cuenta de asegurado")
            }
            if (!registro) {
                Text(
                    "Accesos de demostración para liquidadores",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Estos accesos simulados sirven para evaluar el flujo local y no son autenticación real.",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = { onDemoLiquidador(Liquidador.VEHICULOS) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Liquidador de Vehículos (demo)")
                }
                Button(onClick = { onDemoLiquidador(Liquidador.HOGAR) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Liquidador de Hogar (demo)")
                }
            }
        }
    }
}
