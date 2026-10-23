package cl.duocuc.segurosbpo

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavegacionCamaraPersistenciaTest {
    companion object {
        private const val documento = "Foto integrada QA temporal"

        @JvmStatic
        @BeforeClass
        fun prepararPerfil() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("sesion_demo", 0).edit()
                .putString("asegurado", Asegurado.CAMILA.name).commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private var casoId: Long? = null
    private var ultimaActualizacion = ""
    private var imagen: Bitmap? = null

    @Before fun iniciar() {
        Intents.init()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            context.packageName, Manifest.permission.CAMERA
        )
    }

    @After fun restaurar() {
        Intents.release()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        casoId?.let { id ->
            SiniestroDbHelper(context).use { db ->
                db.writableDatabase.delete(
                    "evidencias",
                    "siniestro_id = ? AND tipo_documento = ?",
                    arrayOf(id.toString(), documento)
                )
                db.writableDatabase.delete(
                    "gestiones",
                    "siniestro_id = ? AND descripcion = ?",
                    arrayOf(id.toString(), "Documento adjuntado: $documento")
                )
                db.writableDatabase.update(
                    "siniestros",
                    ContentValues().apply { put("ultima_actualizacion", ultimaActualizacion) },
                    "id = ?", arrayOf(id.toString())
                )
            }
        }
        imagen?.recycle()
        context.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
    }

    @Test fun fotografiaDesdeNavegacionSePersisteSoloParaAseguradoAutorizado() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val previo = SiniestroDbHelper(context).use { db ->
            val caso = db.listar(Asegurado.CAMILA)
                .first { it.etapa == EtapaSiniestro.RECIBIDO }
            casoId = caso.id
            ultimaActualizacion = caso.ultimaActualizacion
            db.listarEvidencias(caso.id, Asegurado.CAMILA).size
        }
        val bitmap = Bitmap.createBitmap(18, 18, Bitmap.Config.ARGB_8888)
        imagen = bitmap
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(
            android.app.Instrumentation.ActivityResult(
                Activity.RESULT_OK, Intent().putExtra("data", bitmap)
            )
        )
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Estado: Recibido").performClick()
        rule.onNodeWithText("Documentos y evidencias").performScrollTo().performClick()
        rule.onNodeWithText("Tipo de documento *").performTextInput(documento)
        rule.onNodeWithText("Tomar fotografía").performScrollTo().performClick()
        rule.waitUntil(25000) {
            SiniestroDbHelper(context).use { db ->
                db.listarEvidencias(casoId!!, Asegurado.CAMILA)
                    .count { it.tipoDocumento == documento } == 1
            }
        }
        SiniestroDbHelper(context).use { db ->
            assertEquals(previo + 1, db.listarEvidencias(casoId!!, Asegurado.CAMILA).size)
            assertTrue(db.listarEvidencias(casoId!!, Asegurado.JORGE).isEmpty())
        }
        rule.onNodeWithText("Evidencias guardadas: ${previo + 1}/15").assertExists()
        SiniestroDbHelper(context).use { db ->
            val evidencia = db.listarEvidencias(casoId!!, Asegurado.CAMILA)
                .single { it.tipoDocumento == documento }
            val recuperada = db.obtenerImagen(evidencia.id, casoId!!, Asegurado.CAMILA)
            assertNotNull(recuperada)
            recuperada!!.recycle()
        }
        rule.onNodeWithText("Ver imagen").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Ver imagen").assertExists()
    }
}
