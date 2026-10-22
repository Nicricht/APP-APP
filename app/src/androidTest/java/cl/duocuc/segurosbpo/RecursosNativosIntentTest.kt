package cl.duocuc.segurosbpo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecursosNativosIntentTest {
    @get:Rule val composeRule = createComposeRule()

    private val ejemplo = Siniestro(
        id = 99L,
        numero = "SIN-26-009999",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Quilicura",
        descripcion = "Prueba académica",
        huboTerceros = false,
        etapa = EtapaSiniestro.RECIBIDO,
        ultimaActualizacion = "21-10-2026 10:00"
    )

    @Before fun preparar() { Intents.init() }
    @After fun terminar() { Intents.release() }

    @Test fun galeriaAbreSelectorAndroidYSePuedeCancelar() {
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
            android.app.Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
        composeRule.setContent {
            EvidenciasScreen(ejemplo, Asegurado.CAMILA, emptyList(),
                onVolver = {}, onCargar = {}, onCargarImagen = { null },
                onGuardarFoto = { _, _ -> null },
                onGuardarArchivo = { _, _ -> throw AssertionError("No debía guardarse un archivo") })
        }
        composeRule.onNodeWithText("Tipo de documento *").performTextInput("Fotografía")
        composeRule.onNodeWithText("Elegir de la galería").performScrollTo().performClick()
        composeRule.waitUntil(15000) { Intents.getIntents().any { it.action == Intent.ACTION_GET_CONTENT } }
        assertEquals(1, Intents.getIntents().count { it.action == Intent.ACTION_GET_CONTENT })
        composeRule.onNodeWithText("Evidencias guardadas: 0/15").assertExists()
    }

    @Test fun camaraRecibeImagenSimuladaYSolicitaGuardarla() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            context.packageName, Manifest.permission.CAMERA)
        val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        val respuesta = Intent().putExtra("data", bitmap)
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(
            android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, respuesta)
        )
        val guardada = java.util.concurrent.atomic.AtomicBoolean(false)
        composeRule.setContent {
            EvidenciasScreen(ejemplo, Asegurado.CAMILA, emptyList(),
                onVolver = {}, onCargar = {}, onCargarImagen = { null },
                onGuardarFoto = { tipo, imagen ->
                    guardada.set(tipo == "Foto del daño" && imagen.width == 20)
                    null
                },
                onGuardarArchivo = { _, _ -> null })
        }
        composeRule.onNodeWithText("Tipo de documento *").performTextInput("Foto del daño")
        composeRule.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        composeRule.waitUntil(15000) { Intents.getIntents().any { it.action == MediaStore.ACTION_IMAGE_CAPTURE } }
        assertEquals(1, Intents.getIntents().count { it.action == MediaStore.ACTION_IMAGE_CAPTURE })
        composeRule.waitUntil(15000) { guardada.get() }
        assertTrue(guardada.get())
        bitmap.recycle()
    }
}
