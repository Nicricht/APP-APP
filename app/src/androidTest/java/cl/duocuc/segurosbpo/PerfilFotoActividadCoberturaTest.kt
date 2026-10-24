package cl.duocuc.segurosbpo

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class PerfilFotoActividadCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun montar(
        concedido: Boolean = false,
        imagen: Bitmap? = null,
        seleccion: Uri? = null,
        capturas: AtomicInteger = AtomicInteger(0),
        guardadas: AtomicInteger = AtomicInteger(0)
    ) {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val sinPermiso = object : ContextWrapper(contexto) {
            override fun checkSelfPermission(permission: String): Int =
                if (permission == Manifest.permission.CAMERA) PackageManager.PERMISSION_DENIED
                else super.checkSelfPermission(permission)

            override fun checkPermission(permission: String, pid: Int, uid: Int): Int =
                if (permission == Manifest.permission.CAMERA) PackageManager.PERMISSION_DENIED
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
                    is ActivityResultContracts.RequestPermission ->
                        dispatchResult(requestCode, concedido as O)
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
                    onGuardarFoto = {
                        guardadas.incrementAndGet()
                        null
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
}
