package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.ErroresRegistro
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.ValidadorCuenta
import cl.duocuc.segurosbpo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccesoScreen(
    onIniciar: (String, String) -> String?,
    onRegistrar: (String, String, String, String, String, String) -> String?,
    onDemoLiquidador: (Liquidador) -> Unit,
    onDemoAsegurado: () -> Unit
) {
    var registro by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var mostrarErroresRegistro by rememberSaveable { mutableStateOf(false) }
    val erroresRegistro = if (registro && mostrarErroresRegistro) {
        ValidadorCuenta.erroresRegistro(nombre, rut, correo, telefono, clave, confirmacion)
    } else ErroresRegistro()
    val errorNombre = erroresRegistro.nombre
    val errorRut = erroresRegistro.rut
    val errorCorreo = erroresRegistro.correo
    val errorTelefono = erroresRegistro.telefono
    val errorClave = erroresRegistro.clave
    val errorConfirmacion = erroresRegistro.confirmacion

    Scaffold(topBar = { TopAppBar(title = { Text("Seguros BPO") }) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.mi_siniestro_logo),
                contentDescription = "Logo de Seguros BPO",
                modifier = Modifier.size(82.dp).align(Alignment.CenterHorizontally)
            )
            Text(
                if (registro) "Crear cuenta de asegurado" else "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Accede a tus siniestros y documentos registrados en este dispositivo.",
                style = MaterialTheme.typography.bodySmall
            )
            if (registro) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it; mensaje = null },
                    label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(),
                    isError = errorNombre != null,
                    supportingText = if (errorNombre != null) {
                        { Text(errorNombre, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                OutlinedTextField(
                    value = rut, onValueChange = { rut = it; mensaje = null },
                    label = { Text("RUT") }, modifier = Modifier.fillMaxWidth(),
                    isError = errorRut != null,
                    supportingText = if (errorRut != null) {
                        { Text(errorRut, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            OutlinedTextField(
                value = correo, onValueChange = { correo = it; mensaje = null },
                label = { Text("Correo electrónico") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                isError = errorCorreo != null,
                supportingText = if (errorCorreo != null) {
                    { Text(errorCorreo, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            if (registro) {
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it; mensaje = null },
                    label = { Text("Teléfono") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorTelefono != null,
                    supportingText = if (errorTelefono != null) {
                        { Text(errorTelefono, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            OutlinedTextField(
                value = clave, onValueChange = { clave = it; mensaje = null },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = errorClave != null,
                supportingText = if (errorClave != null) {
                    { Text(errorClave, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            if (registro) {
                OutlinedTextField(
                    value = confirmacion, onValueChange = { confirmacion = it; mensaje = null },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorConfirmacion != null,
                    supportingText = if (errorConfirmacion != null) {
                        { Text(errorConfirmacion, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val resultado = if (registro) {
                        onRegistrar(nombre, rut, correo, telefono, clave, confirmacion)
                    } else onIniciar(correo, clave)
                    mensaje = resultado
                    mostrarErroresRegistro = registro && resultado != null
                }
            ) {
                Text(if (registro) "Crear cuenta" else "Iniciar sesión")
            }
            TextButton(
                onClick = {
                    registro = !registro
                    mensaje = null
                    mostrarErroresRegistro = false
                    clave = ""
                    confirmacion = ""
                }
            ) {
                Text(if (registro) "Ya tengo cuenta" else "Crear cuenta de asegurado")
            }
            if (!registro) {
                Button(onClick = onDemoAsegurado, modifier = Modifier.fillMaxWidth()) {
                    Text("Acceder al perfil de Camila Rojas")
                }
                Text(
                    "Perfiles de liquidación disponibles",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Acceso rápido a los perfiles de trabajo configurados en este dispositivo.",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = { onDemoLiquidador(Liquidador.VEHICULOS) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Equipo de vehículos")
                }
                Button(onClick = { onDemoLiquidador(Liquidador.HOGAR) },
                    modifier = Modifier.fillMaxWidth()) {
                    Text("Equipo de hogar")
                }
            }
        }
    }
}
