package cl.duocuc.segurosbpo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.AvatarCircular

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilCuentaScreen(
    cuenta: CuentaLocal,
    onGuardarTelefono: (String) -> String?,
    onGuardarFoto: (Bitmap) -> String?,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val contexto = LocalContext.current
    var telefono by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.telefono) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val foto = remember(cuenta.foto) {
        cuenta.foto?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        imagen ->
        mensaje = if (imagen == null) "Captura cancelada"
        else runCatching { onGuardarFoto(imagen) ?: "Foto actualizada" }
            .getOrElse { it.message ?: "No se pudo guardar la foto" }
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) camara.launch(null) else mensaje = "No se otorgó permiso de cámara"
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) {
            mensaje = "Selección cancelada"
        } else {
            mensaje = runCatching {
                val archivo = ProcesadorEvidencia.desdeSelector(contexto, uri)
                require(archivo.mime.startsWith("image/")) { "Debes seleccionar una imagen" }
                val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(archivo.bytes, 0, archivo.bytes.size, opciones)
                var escala = 1
                while (opciones.outWidth / escala > 600 || opciones.outHeight / escala > 600) {
                    escala *= 2
                }
                opciones.inJustDecodeBounds = false
                opciones.inSampleSize = escala
                val imagen = BitmapFactory.decodeByteArray(
                    archivo.bytes, 0, archivo.bytes.size, opciones
                ) ?: throw IllegalArgumentException("La imagen no se puede abrir")
                runCatching { onGuardarFoto(imagen) ?: "Foto actualizada" }
                    .getOrElse { it.message ?: "No se pudo guardar la foto" }
            }.getOrElse { it.message ?: "No se pudo abrir la imagen" }
        }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Mi perfil") }) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).imePadding().verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver") }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (foto != null) {
                    Image(
                        bitmap = foto, contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                } else {
                    AvatarCircular(imagen = R.drawable.avatar_usuario,
                        descripcion = "Foto predeterminada del perfil", tamano = 100.dp)
                }
                Text(cuenta.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(if (cuenta.rol == RolCuenta.ASEGURADO) "Asegurado" else "Liquidador",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge)
                if (cuenta.equipo != null) Text("Equipo: " + cuenta.equipo,
                    style = MaterialTheme.typography.bodySmall)
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Datos personales", style = MaterialTheme.typography.titleMedium)
                    DatoPerfil("RUT", if (cuenta.rut.isBlank()) "No disponible"
                        else "••••${cuenta.rut.takeLast(5)}")
                    DatoPerfil("Tipo de acceso", "Perfil simulado para pruebas")
                    DatoPerfil("Teléfono", cuenta.telefono.ifBlank { "No registrado" })
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Editar información", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = telefono, onValueChange = { telefono = it },
                        label = { Text("Teléfono de contacto") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Button(onClick = {
                        mensaje = onGuardarTelefono(telefono) ?: "Datos actualizados"
                    }, modifier = Modifier.fillMaxWidth()) { Text("Guardar teléfono") }
                    Text("Foto de perfil", style = MaterialTheme.typography.titleSmall)
                    OutlinedButton(onClick = {
                        if (ContextCompat.checkSelfPermission(
                                contexto, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED) camara.launch(null)
                        else permisoCamara.launch(Manifest.permission.CAMERA)
                    }, modifier = Modifier.fillMaxWidth()) { Text("Tomar una foto") }
                    OutlinedButton(onClick = { galeria.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Elegir de la galería")
                    }
                }
            }
            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
                Text("Cambiar perfil", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DatoPerfil(etiqueta: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
