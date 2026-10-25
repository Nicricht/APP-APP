package cl.duocuc.segurosbpo.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.etiquetaEquipo
import cl.duocuc.segurosbpo.permisos.Permisos
import cl.duocuc.segurosbpo.ui.components.AvatarCuenta
import cl.duocuc.segurosbpo.ui.components.AvatarImagen
import cl.duocuc.segurosbpo.ui.components.BarraSuperior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilCuentaScreen(
    cuenta: CuentaLocal,
    onGuardarFoto: (Bitmap) -> String?,
    onEditar: () -> Unit,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val contexto = LocalContext.current
    var mensaje by remember { mutableStateOf<String?>(null) }
    var permisoRechazado by remember { mutableStateOf(false) }
    var fotoPendiente by remember(cuenta.id) { mutableStateOf<Bitmap?>(null) }

    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { imagen ->
        if (imagen == null) {
            mensaje = "Captura cancelada"
        } else {
            fotoPendiente = imagen
            mensaje = null
        }
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            permisoRechazado = false
            camara.launch(null)
        } else {
            mensaje = "Sin permiso de cámara no se puede tomar la foto de perfil"
            permisoRechazado = true
        }
    }
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            mensaje = "Selección cancelada"
        } else {
            val elegida = runCatching {
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
                BitmapFactory.decodeByteArray(archivo.bytes, 0, archivo.bytes.size, opciones)
                    ?: throw IllegalArgumentException("La imagen no se puede abrir")
            }
            fotoPendiente = elegida.getOrNull()
            mensaje = elegida.exceptionOrNull()?.message
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Mi perfil", onVolver = onVolver) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onEditar) {
                Text("Editar perfil")
            }
        }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).imePadding().verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val pendiente = fotoPendiente
            if (pendiente == null) {
                AvatarCuenta(
                    foto = cuenta.foto,
                    descripcion = "Foto de perfil",
                    tamano = 120.dp
                )
            } else {
                AvatarImagen(
                    imagen = pendiente.asImageBitmap(),
                    descripcion = "Foto de perfil sin guardar",
                    tamano = 120.dp
                )
            }

            Text(cuenta.nombre, style = MaterialTheme.typography.headlineSmall)
            Text(
                when (cuenta.rol) {
                    RolCuenta.ASEGURADO -> "Persona asegurada"
                    RolCuenta.ANALISTA -> "Analista de administración"
                    RolCuenta.LIQUIDADOR ->
                        "Liquidador · equipo " + etiquetaEquipo(cuenta.equipo)
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cuenta.rut.isNotBlank()) {
                        DatoPerfil("RUT", cuenta.rut)
                    }
                    if (cuenta.rol == RolCuenta.ANALISTA) {
                        DatoPerfil("Área", "Administración de siniestros")
                    }
                    DatoPerfil("Correo", cuenta.correo.ifBlank { "Sin correo" })
                    DatoPerfil("Teléfono", cuenta.telefono.ifBlank { "Sin teléfono" })
                }
            }

            Text("Foto de perfil", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (Permisos.tiene(contexto, Permisos.CAMARA)) camara.launch(null)
                        else permisoCamara.launch(Permisos.CAMARA)
                    }
                ) { Text("Cámara") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { galeria.launch("image/*") }
                ) { Text("Galería") }
            }

            if (pendiente != null) {
                Text(
                    "Foto sin guardar",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            fotoPendiente = null
                            mensaje = null
                        }
                    ) { Text("Descartar") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val error = runCatching { onGuardarFoto(pendiente) }
                                .getOrElse { it.message ?: "No se pudo guardar la foto" }
                            mensaje = error ?: "Foto actualizada"
                            if (error == null) fotoPendiente = null
                        }
                    ) { Text("Guardar") }
                }
            }

            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            if (permisoRechazado) {
                OutlinedButton(onClick = { Permisos.abrirAjustes(contexto) }) {
                    Text("Ir a Ajustes")
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCerrarSesion
            ) { Text("Cambiar de usuario") }
        }
    }
}

@Composable
private fun DatoPerfil(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
