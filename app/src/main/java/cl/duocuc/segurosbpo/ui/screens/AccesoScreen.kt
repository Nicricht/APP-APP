package cl.duocuc.segurosbpo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.ErroresRegistro
import cl.duocuc.segurosbpo.model.ValidadorCuenta
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.CampoContrasena

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccesoScreen(
    onIniciar: (String, String) -> String?,
    onRegistrar: (String, String, String, String, String, String) -> String?
) {
    val contexto = LocalContext.current
    var registro by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var mostrarClave by rememberSaveable { mutableStateOf(false) }
    var mostrarConfirmacion by rememberSaveable { mutableStateOf(false) }
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
                .imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.mi_siniestro_logo),
                contentDescription = "Logo de Seguros BPO",
                modifier = Modifier.size(82.dp).align(Alignment.CenterHorizontally)
            )
            Text(
                if (registro) "Crear cuenta" else "Bienvenido a Seguros BPO",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                if (registro) "Ingresa tus datos para comenzar a gestionar tus siniestros."
                else "Consulta tus siniestros, documentos y próximos pasos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    placeholder = { Text("12.345.678-5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    isError = errorRut != null,
                    supportingText = if (errorRut != null) {
                        { Text(errorRut, color = MaterialTheme.colorScheme.error) }
                    } else { { Text("Ejemplo: 12.345.678-5") } }
                )
            }
            OutlinedTextField(
                value = correo, onValueChange = { correo = it; mensaje = null },
                label = { Text("Correo electrónico") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = errorTelefono != null,
                    supportingText = if (errorTelefono != null) {
                        { Text(errorTelefono, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
            CampoContrasena(
                valor = clave, onValorCambia = { clave = it; mensaje = null },
                etiqueta = "Contraseña",
                visible = mostrarClave,
                onAlternarVisible = { mostrarClave = !mostrarClave },
                modifier = Modifier.fillMaxWidth(),
                error = errorClave
            )
            if (registro) {
                CampoContrasena(
                    valor = confirmacion,
                    onValorCambia = { confirmacion = it; mensaje = null },
                    etiqueta = "Confirmar contraseña",
                    visible = mostrarConfirmacion,
                    onAlternarVisible = { mostrarConfirmacion = !mostrarConfirmacion },
                    modifier = Modifier.fillMaxWidth(),
                    error = errorConfirmacion
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
                    if (registro && resultado == null) {
                        Toast.makeText(contexto, "Cuenta creada correctamente", Toast.LENGTH_LONG).show()
                    }
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
                    mostrarClave = false
                    mostrarConfirmacion = false
                }
            ) {
                Text(if (registro) "Ya tengo cuenta" else "Crear cuenta de asegurado")
            }
            if (!registro) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Acceso para liquidadores", style = MaterialTheme.typography.titleMedium)
                        Text("Si eres liquidador, inicia sesión con el correo y la contraseña asignados a tu equipo. El registro público es solo para asegurados.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
