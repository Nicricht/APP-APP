package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.ValidadorContacto
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.ui.components.BarraSuperior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    cuenta: CuentaLocal,
    onGuardar: (String, String) -> String?,
    onVolver: () -> Unit
) {
    var correo by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.correo) }
    var telefono by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.telefono) }
    var tocoCorreo by rememberSaveable(cuenta.id) { mutableStateOf(false) }
    var tocoTelefono by rememberSaveable(cuenta.id) { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var guardado by remember { mutableStateOf(false) }

    val errorCorreo = ValidadorContacto.errorCorreo(correo)
    val errorTelefono = ValidadorContacto.errorTelefono(telefono)
    val hayCambios = ValidadorContacto.normalizarCorreo(correo) != cuenta.correo ||
        ValidadorContacto.normalizarTelefono(telefono) != cuenta.telefono
    val puedeGuardar = hayCambios && errorCorreo == null && errorTelefono == null

    Scaffold(topBar = { BarraSuperior("Editar perfil", onVolver = onVolver) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Datos de contacto", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = cuenta.nombre,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if (cuenta.rut.isBlank()) "Sin RUT asociado"
                    else ValidadorRut.ocultar(cuenta.rut),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("RUT") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    tocoCorreo = true
                    mensaje = null
                    guardado = false
                },
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = tocoCorreo && errorCorreo != null,
                supportingText = {
                    Text(
                        if (tocoCorreo && errorCorreo != null) errorCorreo
                        else "Ejemplo: nombre@correo.cl"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    tocoTelefono = true
                    mensaje = null
                    guardado = false
                },
                label = { Text("Teléfono de contacto") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = tocoTelefono && errorTelefono != null,
                supportingText = {
                    Text(
                        if (tocoTelefono && errorTelefono != null) errorTelefono
                        else "Solo números, entre 8 y 11 dígitos"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = hayCambios,
                    onClick = {
                        correo = cuenta.correo
                        telefono = cuenta.telefono
                        tocoCorreo = false
                        tocoTelefono = false
                        mensaje = null
                        guardado = false
                    }
                ) { Text("Descartar") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = puedeGuardar,
                    onClick = {
                        val error = onGuardar(correo, telefono)
                        guardado = error == null
                        mensaje = error ?: "Datos actualizados"
                    }
                ) { Text("Guardar") }
            }

            if (!hayCambios && mensaje == null) {
                Text(
                    "No hay cambios por guardar.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            mensaje?.let {
                Text(
                    it,
                    color = if (guardado) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
