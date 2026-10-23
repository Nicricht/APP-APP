package cl.duocuc.segurosbpo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class RespuestasSelectorEvidenciaTest {
    @get:Rule val rule = createComposeRule()
    private val caso = Siniestro(
        id = 54, numero = "SIN-26-000054", asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA, tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026", fechaReporte = "21-10-2026",
        comuna = "Maipú", descripcion = "Siniestro de prueba", huboTerceros = false,
        etapa = EtapaSiniestro.RECIBIDO, ultimaActualizacion = "22-10-2026 14:00"
    )

    @Before fun iniciar() { Intents.init() }
    @After fun finalizar() { Intents.release() }

    private fun mostrar(onArchivo: suspend (String, Uri) -> String? = { _, _ -> null },
                        onFoto: suspend (String, Bitmap) -> String? = { _, _ -> null }) {
        rule.setContent {
            EvidenciasScreen(
                siniestro = caso, asegurado = Asegurado.CAMILA,
                evidencias = emptyList(),
                onVolver = {}, onCargar = {}, onCargarImagen = { null },
                onGuardarFoto = onFoto, onGuardarArchivo = onArchivo
            )
        }
    }

    private fun mensajeVisible(mensaje: String) {
        rule.waitUntil(15000) {
            rule.onAllNodesWithText(mensaje).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun seleccionPdfValidoMuestraGuardadoExitoso() {
        val uri = Uri.parse("content://cl.duocuc.segurosbpo.qa.archivos/constancia")
        val recibido = AtomicReference("")
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
            android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(uri))
        )
        mostrar(onArchivo = { tipo, archivo ->
            recibido.set("$tipo:$archivo")
            null
        })
        rule.onNodeWithText("Tipo de documento *").performTextInput("Constancia")
        rule.onNodeWithText("Adjuntar PDF").performScrollTo().performClick()
        rule.waitUntil(15000) { recibido.get().isNotBlank() }
        mensajeVisible("Documento guardado en este teléfono")
        assertEquals("Constancia:$uri", recibido.get())
        assertEquals(1, Intents.getIntents().count { it.action == Intent.ACTION_GET_CONTENT })
    }

    @Test fun selectorDeGaleriaPropagaErrorEnPantalla() {
        val uri = Uri.parse("content://cl.duocuc.segurosbpo.qa.archivos/dano")
        val recibido = AtomicReference("")
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
            android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(uri))
        )
        mostrar(onArchivo = { tipo, archivo ->
            recibido.set("$tipo:$archivo")
            "Imagen incompatible"
        })
        rule.onNodeWithText("Tipo de documento *").performTextInput("Fotografía")
        rule.onNodeWithText("Elegir de la galería").performScrollTo().performClick()
        rule.waitUntil(15000) { recibido.get().isNotBlank() }
        mensajeVisible("Imagen incompatible")
        assertEquals("Fotografía:$uri", recibido.get())
    }

    @Test fun cancelarPdfNoGuardaElArchivo() {
        val llamadas = AtomicInteger(0)
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
            android.app.Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
        mostrar(onArchivo = { _, _ -> llamadas.incrementAndGet(); null })
        rule.onNodeWithText("Tipo de documento *").performTextInput("Informe")
        rule.onNodeWithText("Adjuntar PDF").performScrollTo().performClick()
        rule.waitUntil(15000) { Intents.getIntents().any { it.action == Intent.ACTION_GET_CONTENT } }
        rule.waitForIdle()
        assertEquals(0, llamadas.get())
    }

    @Test fun cancelarCapturaMuestraAvisoSinGuardarFotos() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            context.packageName, Manifest.permission.CAMERA
        )
        val llamadas = AtomicInteger(0)
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(
            android.app.Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        )
        mostrar(onFoto = { _, _ -> llamadas.incrementAndGet(); null })
        rule.onNodeWithText("Tipo de documento *").performTextInput("Foto del daño")
        rule.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        rule.waitUntil(15000) { Intents.getIntents().any { it.action == MediaStore.ACTION_IMAGE_CAPTURE } }
        mensajeVisible("Captura cancelada")
        assertEquals(0, llamadas.get())
    }
}
