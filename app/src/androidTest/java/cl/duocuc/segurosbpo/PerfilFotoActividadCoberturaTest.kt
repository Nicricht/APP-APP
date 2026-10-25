package cl.duocuc.segurosbpo

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.ui.screens.PerfilCuentaScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class PerfilFotoActividadCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun montar(
        concedido: Boolean = false,
        imagen: Bitmap? = null,
        seleccion: Uri? = null,
        capturas: AtomicInteger = AtomicInteger(0),
        guardadas: AtomicInteger = AtomicInteger(0),
        errorFoto: String? = null,
        excepcionAlGuardar: Throwable? = null,
        permisoPrevio: Boolean = false,
        peticionesPermiso: AtomicInteger = AtomicInteger(0),
        validarImagen: ((Bitmap) -> Unit)? = null
    ) {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val sinPermiso = object : ContextWrapper(contexto) {
            override fun checkSelfPermission(permission: String): Int =
                if (permission == Manifest.permission.CAMERA) {
                    if (permisoPrevio) PackageManager.PERMISSION_GRANTED
                    else PackageManager.PERMISSION_DENIED
                }
                else super.checkSelfPermission(permission)

            override fun checkPermission(permission: String, pid: Int, uid: Int): Int =
                if (permission == Manifest.permission.CAMERA) {
                    if (permisoPrevio) PackageManager.PERMISSION_GRANTED
                    else PackageManager.PERMISSION_DENIED
                }
                else super.checkPermission(permission, pid, uid)
        }
        val registro = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                when (contract) {
                    is ActivityResultContracts.RequestPermission -> {
                        peticionesPermiso.incrementAndGet()
                        dispatchResult(requestCode, concedido as O)
                    }
                    is ActivityResultContracts.TakePicturePreview -> {
                        capturas.incrementAndGet()
                        dispatchResult(requestCode, imagen as O)
                    }
                    is ActivityResultContracts.GetContent ->
                        dispatchResult(requestCode, seleccion as O)
                    else -> throw AssertionError("Contrato inesperado")
                }
            }
        }
        val propietario = object : ActivityResultRegistryOwner {
            override val activityResultRegistry: ActivityResultRegistry = registro
        }
        val cuenta = CuentaLocal(
            id = "USR_FOTO_QA",
            nombre = "Cuenta de prueba",
            rut = "12.345.678-5",
            correo = "foto@ejemplo.cl",
            telefono = "912345678",
            rol = RolCuenta.ASEGURADO,
            equipo = null
        )
        rule.setContent {
            CompositionLocalProvider(
                LocalContext provides sinPermiso,
                LocalActivityResultRegistryOwner provides propietario
            ) {
                PerfilCuentaScreen(
                    cuenta = cuenta,
                    onGuardarTelefono = { null },
                    onGuardarFoto = { foto ->
                        validarImagen?.invoke(foto)
                        guardadas.incrementAndGet()
                        excepcionAlGuardar?.let { throw it }
                        errorFoto
                    },
                    onVolver = {},
                    onCerrarSesion = {}
                )
            }
        }
    }

    @Test fun denegarPermisoNoAbreCamaraNiGuardaImagen() {
        val capturas = AtomicInteger()
        val guardadas = AtomicInteger()
        montar(concedido = false, capturas = capturas, guardadas = guardadas)
        rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
        rule.onNodeWithText("No se otorgó permiso de cámara").assertExists()
        assertEquals(0, capturas.get())
        assertEquals(0, guardadas.get())
    }

    @Test fun cancelarCamaraPresentaResultadoSinGuardarImagen() {
        val capturas = AtomicInteger()
        val guardadas = AtomicInteger()
        montar(concedido = true, capturas = capturas, guardadas = guardadas)
        rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
        rule.onNodeWithText("Captura cancelada").assertExists()
        assertEquals(1, capturas.get())
        assertEquals(0, guardadas.get())
    }

    @Test fun guardarFotoDesdeCamaraMuestraConfirmacion() {
        val foto = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            val capturas = AtomicInteger()
            val guardadas = AtomicInteger()
            montar(concedido = true, imagen = foto, capturas = capturas, guardadas = guardadas)
            rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
            rule.onNodeWithText("Foto actualizada").assertExists()
            assertEquals(1, capturas.get())
            assertEquals(1, guardadas.get())
        } finally {
            foto.recycle()
        }
    }

    @Test fun cancelarGaleriaNoGuardaFoto() {
        val guardadas = AtomicInteger()
        montar(guardadas = guardadas)
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("Selección cancelada").assertExists()
        assertEquals(0, guardadas.get())
    }

    private fun archivo(mime: String, contenido: ByteArray): Uri = Uri.Builder()
        .scheme("content")
        .authority("cl.duocuc.segurosbpo.qa.archivos")
        .appendPath("documento")
        .appendQueryParameter("mime", mime)
        .appendQueryParameter("nombre", "perfil")
        .appendQueryParameter(
            "contenido",
            Base64.encodeToString(contenido, Base64.URL_SAFE or Base64.NO_WRAP)
        )
        .build()

    @Test fun galeriaGuardaJpegValidoYConfirmaElResultado() {
        val imagen = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        try {
            imagen.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        } finally {
            imagen.recycle()
        }
        val guardadas = AtomicInteger()
        montar(seleccion = archivo("image/jpeg", salida.toByteArray()), guardadas = guardadas)
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("Foto actualizada").assertExists()
        assertEquals(1, guardadas.get())
    }

    @Test fun galeriaNoPermiteSeleccionarPdfComoFotoDePerfil() {
        val guardadas = AtomicInteger()
        montar(seleccion = archivo("application/pdf", "%PDF-1.4".toByteArray()), guardadas = guardadas)
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("Debes seleccionar una imagen").assertExists()
        assertEquals(0, guardadas.get())
    }

    @Test fun galeriaRechazaContenidoConFirmaDistintaAlMime() {
        val guardadas = AtomicInteger()
        montar(seleccion = archivo("image/jpeg", "%PDF-1.4".toByteArray()), guardadas = guardadas)
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("El contenido del archivo no coincide con su formato").assertExists()
        assertEquals(0, guardadas.get())
    }

    @Test fun galeriaRechazaJpegNoDecodificable() {
        val guardadas = AtomicInteger()
        val contenido = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())
        montar(seleccion = archivo("image/jpeg", contenido), guardadas = guardadas)
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("La imagen no se puede abrir").assertExists()
        assertEquals(0, guardadas.get())
    }

    @Test fun errorAlGuardarDesdeCamaraNoSeConfundeConFotoActualizada() {
        val foto = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        try {
            val guardadas = AtomicInteger()
            montar(concedido = true, imagen = foto, guardadas = guardadas,
                errorFoto = "No se pudo guardar la foto")
            rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
            rule.onNodeWithText("No se pudo guardar la foto").assertExists()
            rule.onNodeWithText("Foto actualizada").assertDoesNotExist()
            assertEquals(1, guardadas.get())
        } finally {
            foto.recycle()
        }
    }

    @Test fun errorAlGuardarDesdeGaleriaNoSeConfundeConExito() {
        val imagen = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        try {
            imagen.compress(Bitmap.CompressFormat.JPEG, 88, salida)
        } finally {
            imagen.recycle()
        }
        val guardadas = AtomicInteger()
        montar(
            seleccion = archivo("image/jpeg", salida.toByteArray()),
            guardadas = guardadas,
            errorFoto = "La foto no puede superar 1 MB"
        )
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("La foto no puede superar 1 MB").assertExists()
        rule.onNodeWithText("Foto actualizada").assertDoesNotExist()
        assertEquals(1, guardadas.get())
    }

    @Test fun permisoYaOtorgadoAbreCamaraSinPedirAutorizacionOtraVez() {
        val foto = Bitmap.createBitmap(22, 22, Bitmap.Config.ARGB_8888)
        try {
            val capturas = AtomicInteger()
            val guardadas = AtomicInteger()
            val peticiones = AtomicInteger()
            montar(
                imagen = foto,
                capturas = capturas,
                guardadas = guardadas,
                permisoPrevio = true,
                peticionesPermiso = peticiones
            )
            rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
            rule.onNodeWithText("Foto actualizada").assertExists()
            assertEquals(0, peticiones.get())
            assertEquals(1, capturas.get())
            assertEquals(1, guardadas.get())
        } finally {
            foto.recycle()
        }
    }

    @Test fun permisoAnteriorNoEximeDeInformarCapturaCancelada() {
        val capturas = AtomicInteger()
        val guardadas = AtomicInteger()
        val peticiones = AtomicInteger()
        montar(
            permisoPrevio = true,
            capturas = capturas,
            guardadas = guardadas,
            peticionesPermiso = peticiones
        )
        rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
        rule.onNodeWithText("Captura cancelada").assertExists()
        assertEquals(0, peticiones.get())
        assertEquals(1, capturas.get())
        assertEquals(0, guardadas.get())
    }

    @Test fun fotoGrandeDeGaleriaSeReduceAntesDeGuardarse() {
        val original = Bitmap.createBitmap(1600, 1200, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        try {
            original.eraseColor(android.graphics.Color.BLUE)
            original.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        } finally {
            original.recycle()
        }
        val dimensiones = AtomicReference<Pair<Int, Int>?>()
        val guardadas = AtomicInteger()
        montar(
            seleccion = archivo("image/jpeg", salida.toByteArray()),
            guardadas = guardadas,
            validarImagen = { imagen ->
                dimensiones.set(imagen.width to imagen.height)
            }
        )
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("Foto actualizada").assertExists()
        assertEquals(1, guardadas.get())
        val tamano = dimensiones.get() ?: throw AssertionError("No se entregó ninguna imagen")
        assertTrue(tamano.first in 1..600)
        assertTrue(tamano.second in 1..600)
    }

    @Test fun fotoVerticalAltaSeReduceAunqueSuAnchoNoSupereElLimite() {
        val original = Bitmap.createBitmap(400, 1600, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        try {
            original.eraseColor(android.graphics.Color.BLUE)
            original.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        } finally {
            original.recycle()
        }
        val dimensiones = AtomicReference<Pair<Int, Int>?>()
        val guardadas = AtomicInteger()
        montar(
            seleccion = archivo("image/jpeg", salida.toByteArray()),
            guardadas = guardadas,
            validarImagen = { imagen -> dimensiones.set(imagen.width to imagen.height) }
        )
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("Foto actualizada").assertExists()
        assertEquals(1, guardadas.get())
        val tamano = dimensiones.get() ?: throw AssertionError("No se guardó la fotografía vertical")
        assertTrue("Ancho excesivo: ${tamano.first}", tamano.first in 1..600)
        assertTrue("Alto excesivo: ${tamano.second}", tamano.second in 1..600)
        assertTrue("La foto debe conservar orientación vertical", tamano.second > tamano.first)
    }

    @Test fun excepcionDeGuardadoEnCamaraMuestraErrorSinCerrarPerfil() {
        val imagen = Bitmap.createBitmap(18, 18, Bitmap.Config.ARGB_8888)
        try {
            val guardadas = AtomicInteger()
            montar(
                concedido = true,
                imagen = imagen,
                guardadas = guardadas,
                excepcionAlGuardar = IllegalStateException("Error al guardar la foto")
            )
            rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
            rule.onNodeWithText("Error al guardar la foto").assertExists()
            rule.onNodeWithText("Foto actualizada").assertDoesNotExist()
            rule.onNodeWithText("Cuenta de prueba").assertExists()
            assertEquals(1, guardadas.get())
        } finally {
            imagen.recycle()
        }
    }

    @Test fun errorDeCamaraSinMensajeTieneRespuestaComprensible() {
        val imagen = Bitmap.createBitmap(18, 18, Bitmap.Config.ARGB_8888)
        try {
            montar(
                concedido = true,
                imagen = imagen,
                excepcionAlGuardar = IllegalStateException()
            )
            rule.onNodeWithText("Tomar foto de perfil").performScrollTo().performClick()
            rule.onNodeWithText("No se pudo guardar la foto").assertExists()
            rule.onNodeWithText("Foto actualizada").assertDoesNotExist()
        } finally {
            imagen.recycle()
        }
    }

    @Test fun errorDeGuardadoDeGaleriaNoSeConfundeConImagenCorrupta() {
        val imagen = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        try {
            imagen.compress(Bitmap.CompressFormat.JPEG, 86, salida)
        } finally {
            imagen.recycle()
        }
        val guardadas = AtomicInteger()
        montar(
            seleccion = archivo("image/jpeg", salida.toByteArray()),
            guardadas = guardadas,
            excepcionAlGuardar = IllegalStateException()
        )
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("No se pudo guardar la foto").assertExists()
        rule.onNodeWithText("La imagen no se puede abrir").assertDoesNotExist()
        rule.onNodeWithText("Foto actualizada").assertDoesNotExist()
        assertEquals(1, guardadas.get())
    }

    @Test fun errorConMensajeDeGaleriaSeMuestraSinInformarExito() {
        val imagen = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        try {
            imagen.compress(Bitmap.CompressFormat.JPEG, 86, salida)
        } finally {
            imagen.recycle()
        }
        montar(
            seleccion = archivo("image/jpeg", salida.toByteArray()),
            excepcionAlGuardar = IllegalStateException("Error de almacenamiento")
        )
        rule.onNodeWithText("Elegir foto de la galería").performScrollTo().performClick()
        rule.onNodeWithText("Error de almacenamiento").assertExists()
        rule.onNodeWithText("Foto actualizada").assertDoesNotExist()
    }
}
