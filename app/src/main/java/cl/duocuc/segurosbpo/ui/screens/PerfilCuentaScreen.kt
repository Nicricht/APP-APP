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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta

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
    val editable = cuenta.correo.isNotBlank()
    var telefono by rememberSaveable(cuenta.id) { mutableStateOf(cuenta.telefono) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val foto = remember(cuenta.foto) {
        cuenta.foto?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        imagen -> if (imagen != null) mensaje = onGuardarFoto(imagen)
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) camara.launch(null) else mensaje = "No se otorgó permiso de cámara"
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
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
                onGuardarFoto(imagen)
            }.getOrElse { it.message ?: "No se pudo abrir la imagen" }
        }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Mi perfil") }) }) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextButton(onClick = onVolver) { Text("Volver") }
            foto?.let {
                Image(
                    bitmap = it, contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            }
            Text(cuenta.nombre, style = MaterialTheme.typography.headlineSmall)
            Text(if (cuenta.rol == RolCuenta.ASEGURADO) "Asegurado" else "Liquidador")
            if (cuenta.equipo != null) Text("Equipo: " + cuenta.equipo)
            if (cuenta.correo.isNotEmpty()) Text("Correo: " + cuenta.correo)
            if (editable) {
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono de contacto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    mensaje = onGuardarTelefono(telefono) ?: "Datos actualizados"
                }) { Text("Guardar teléfono") }
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(
                            contexto, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED) camara.launch(null)
                    else permisoCamara.launch(Manifest.permission.CAMERA)
                }) { Text("Tomar foto de perfil") }
                Button(onClick = { galeria.launch("image/*") }) {
                    Text("Elegir foto de la galería")
                }
            } else {
                Text("Perfil ficticio de demostración. Los datos no son editables.")
            }
            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Button(onClick = onCerrarSesion) { Text("Cerrar sesión") }
        }
    }
}
