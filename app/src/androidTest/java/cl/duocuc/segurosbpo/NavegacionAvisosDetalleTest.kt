package cl.duocuc.segurosbpo

import android.content.ContentValues
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavegacionAvisosDetalleTest {
    companion object {
        private const val mensaje = "Aviso temporal QA para consultar el siniestro"

        @JvmStatic
        @BeforeClass
        fun prepararPerfil() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("sesion_demo", 0).edit()
                .putString("asegurado", Asegurado.CAMILA.name).commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private var avisoId: Long? = null

    @After fun limpiarAvisoTemporal() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        avisoId?.let { id ->
            SiniestroDbHelper(context).use { db ->
                db.writableDatabase.delete(
                    "avisos", "id = ?", arrayOf(id.toString())
                )
            }
        }
        context.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
    }

    @Test fun abrirAvisoMarcaLeidoYNavegaAlCasoDelAsegurado() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val caso = SiniestroDbHelper(context).use { db ->
            val seleccionado = db.listar(Asegurado.CAMILA).first()
            avisoId = db.writableDatabase.insertOrThrow(
                "avisos", null, ContentValues().apply {
                    put("siniestro_id", seleccionado.id)
                    put("mensaje", mensaje)
                    put("fecha", "22-10-2026 22:00")
                    put("leido", 0)
                }
            )
            seleccionado
        }
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Avisos", substring = true).performClick()
        rule.waitUntil(16000) {
            rule.onAllNodesWithText(mensaje).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onAllNodesWithText("Ver siniestro")[0].performClick()
        rule.onNodeWithText("Detalle del siniestro").assertExists()
        rule.onNodeWithText(caso.numero).assertExists()
        SiniestroDbHelper(context).use { db ->
            assertTrue(db.listarAvisos(Asegurado.CAMILA).any {
                it.id == avisoId && it.leido
            })
        }
    }
}
