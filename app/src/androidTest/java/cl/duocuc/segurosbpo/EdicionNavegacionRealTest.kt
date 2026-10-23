package cl.duocuc.segurosbpo

import android.content.ContentValues
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EdicionNavegacionRealTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun seleccionarPerfil() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("sesion_demo", 0).edit()
                .putString("asegurado", Asegurado.CAMILA.name).commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private var casoId: Long? = null
    private var descripcionAnterior = ""
    private var actualizacionAnterior = ""
    private var gestionAnterior = 0L

    @After fun restaurarRegistro() {
        casoId?.let { id ->
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val db = SiniestroDbHelper(context)
            try {
                db.writableDatabase.update(
                    "siniestros",
                    ContentValues().apply {
                        put("descripcion", descripcionAnterior)
                        put("ultima_actualizacion", actualizacionAnterior)
                    },
                    "id = ?", arrayOf(id.toString())
                )
                db.writableDatabase.delete(
                    "gestiones",
                    "siniestro_id = ? AND id > ?",
                    arrayOf(id.toString(), gestionAnterior.toString())
                )
            } finally {
                db.close()
            }
        }
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
    }

    @Test fun guardarCorreccionDesdeDetalleRegresaALaLista() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = SiniestroDbHelper(context)
        try {
            val original = db.listar(Asegurado.CAMILA).first { it.etapa == EtapaSiniestro.RECIBIDO }
            casoId = original.id
            descripcionAnterior = original.descripcion
            actualizacionAnterior = original.ultimaActualizacion
            db.readableDatabase.rawQuery(
                "SELECT COALESCE(MAX(id), 0) FROM gestiones WHERE siniestro_id = ?",
                arrayOf(original.id.toString())
            ).use { cursor ->
                assertTrue(cursor.moveToFirst())
                gestionAnterior = cursor.getLong(0)
            }
        } finally {
            db.close()
        }

        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Estado: Recibido").performClick()
        rule.onNodeWithText("Corregir descripción").performScrollTo().performClick()
        rule.onNodeWithText("Corregir reporte").assertExists()
        rule.onNodeWithText("Descripción").performTextClearance()
        rule.onNodeWithText("Descripción").performTextInput("Descripción validada por navegación QA")
        rule.onNodeWithText("Guardar cambios").performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()

        val verificacion = SiniestroDbHelper(context)
        try {
            assertEquals(
                "Descripción validada por navegación QA",
                verificacion.obtener(casoId!!, Asegurado.CAMILA)?.descripcion
            )
        } finally {
            verificacion.close()
        }
    }
}
