package cl.duocuc.segurosbpo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavegacionDocumentosSistemaTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun prepararPerfil() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("sesion_demo", 0).edit()
                .putString("asegurado", Asegurado.CAMILA.name).commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun iniciar() { Intents.init() }

    @After fun finalizar() {
        Intents.release()
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
    }

    @Test fun seleccionPdfNoAdmitidoMuestraErrorSinGuardarArchivos() {
        if (rule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("Cerrar sesión").performClick()
        }
        rule.onNodeWithText("Continuar como Camila (demo)").performClick()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = SiniestroDbHelper(context)
        val caso = try {
            db.listar(Asegurado.CAMILA).first { it.etapa == EtapaSiniestro.RECIBIDO }
        } finally {
            db.close()
        }
        val inicial = SiniestroDbHelper(context).use {
            it.listarEvidencias(caso.id, Asegurado.CAMILA).size
        }
        val archivo = Uri.Builder()
            .scheme("content")
            .authority("cl.duocuc.segurosbpo.qa.archivos")
            .appendPath("archivo-invalido")
            .appendQueryParameter("mime", "text/plain")
            .build()
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(
            android.app.Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                Intent().setData(archivo)
            )
        )
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Estado: Recibido").performClick()
        rule.onNodeWithText("Documentos y evidencias").performScrollTo().performClick()
        rule.onNodeWithText("Tipo de documento *").performTextInput("Archivo ajeno")
        rule.onNodeWithText("Adjuntar PDF").performScrollTo().performClick()
        rule.waitUntil(20000) {
            rule.onAllNodesWithText("Selecciona una imagen JPG, PNG, WEBP o un PDF")
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertTrue(Intents.getIntents().any { it.action == Intent.ACTION_GET_CONTENT })
        val actual = SiniestroDbHelper(context).use {
            it.listarEvidencias(caso.id, Asegurado.CAMILA).size
        }
        assertEquals(inicial, actual)
        rule.onNodeWithText("Volver al siniestro").performScrollTo().performClick()
        rule.onNodeWithText("Detalle del siniestro").assertExists()
    }
}
