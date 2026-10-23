package cl.duocuc.segurosbpo

import android.Manifest
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class PermisosCamaraCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private val caso = Siniestro(
        id = 454, numero = "SIN-26-000454", asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA, tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026", fechaReporte = "21-10-2026",
        comuna = "Santiago", descripcion = "Prueba del permiso de cámara",
        huboTerceros = false, etapa = EtapaSiniestro.RECIBIDO,
        ultimaActualizacion = "22-10-2026 18:00"
    )

    private fun abrirConPermisoSimulado(
        concedido: Boolean,
        foto: Bitmap? = null,
        resultado: String? = null,
        fotoGuardada: AtomicReference<String>? = null,
        capturas: AtomicInteger? = null
    ) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val sinPermiso = object : ContextWrapper(base) {
            override fun checkSelfPermission(permission: String): Int =
                if (permission == Manifest.permission.CAMERA) PackageManager.PERMISSION_DENIED
                else super.checkSelfPermission(permission)

            override fun checkPermission(permission: String, pid: Int, uid: Int): Int =
                if (permission == Manifest.permission.CAMERA) PackageManager.PERMISSION_DENIED
                else super.checkPermission(permission, pid, uid)
        }
        val registry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                when (contract) {
                    is ActivityResultContracts.RequestPermission -> dispatchResult(requestCode, concedido as O)
                    is ActivityResultContracts.TakePicturePreview -> {
                        capturas?.incrementAndGet()
                        dispatchResult(requestCode, foto as O)
                    }
                    else -> throw AssertionError("Contrato de actividad inesperado")
                }
            }
        }
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry: ActivityResultRegistry = registry
        }
        rule.setContent {
            CompositionLocalProvider(
                LocalContext provides sinPermiso,
                LocalActivityResultRegistryOwner provides owner
            ) {
                EvidenciasScreen(
                    siniestro = caso, asegurado = Asegurado.CAMILA,
                    evidencias = emptyList(),
                    onVolver = {}, onCargar = {}, onCargarImagen = { null },
                    onGuardarFoto = { tipo, bitmap ->
                        fotoGuardada?.set("$tipo:${bitmap.width}")
                        resultado
                    },
                    onGuardarArchivo = { _, _ -> null }
                )
            }
        }
        rule.onNodeWithText("Tipo de documento *").performTextInput("Fotografía")
        rule.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
    }

    private fun mensaje(mensaje: String) {
        rule.waitUntil(15000) {
            rule.onAllNodesWithText(mensaje).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun permisoDenegadoMuestraErrorSinAbrirCamara() {
        val capturas = AtomicInteger(0)
        abrirConPermisoSimulado(concedido = false, capturas = capturas)
        mensaje("Permiso de cámara denegado")
        assertEquals(0, capturas.get())
    }

    @Test fun permisoConcedidoPeroCapturaCanceladaInformaCancelacion() {
        val capturas = AtomicInteger(0)
        abrirConPermisoSimulado(concedido = true, foto = null, capturas = capturas)
        mensaje("Captura cancelada")
        assertEquals(1, capturas.get())
    }

    @Test fun permisoConcedidoYFotoRechazadaInformaErrorDeGuardado() {
        val imagen = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        try {
            val guardada = AtomicReference("")
            abrirConPermisoSimulado(
                concedido = true, foto = imagen, resultado = "Foto no guardada",
                fotoGuardada = guardada
            )
            mensaje("Foto no guardada")
            assertEquals("Fotografía:24", guardada.get())
        } finally {
            imagen.recycle()
        }
    }
}
