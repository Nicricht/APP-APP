package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
class RegistroNavegacionRealTest {
    companion object {
        private const val descripcionPrueba = "Registro de navegación temporal QA octubre 2026"

        @JvmStatic
        @BeforeClass
        fun prepararAsegurado() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("sesion_demo", 0).edit()
                .putString("asegurado", Asegurado.CAMILA.name).commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @After fun limpiarCasoCreado() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = SiniestroDbHelper(context)
        try {
            val registros = db.listar(Asegurado.CAMILA).filter { it.descripcion == descripcionPrueba }
            for (siniestro in registros) {
                db.writableDatabase.delete(
                    "siniestros", "id = ? AND asegurado = ?",
                    arrayOf(siniestro.id.toString(), Asegurado.CAMILA.name)
                )
            }
        } finally {
            db.close()
        }
        context.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
    }

    @Test fun reporteDesdeLaListaSeRegistraYRegresaALaPantallaPrincipal() {
        if (rule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("Cerrar sesión").performClick()
        }
        rule.onNodeWithText("Continuar como Camila (demo)").performClick()

        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Reportar").performClick()
        rule.onNodeWithText("Reportar un siniestro").assertExists()
        rule.onNodeWithText("Fecha en que ocurrió (día-mes-año)")
            .performScrollTo().performTextInput("20-10-2026")
        rule.onNodeWithText("Comuna donde ocurrió")
            .performScrollTo().performTextInput("Santiago")
        rule.onNodeWithText("Cuéntanos brevemente qué pasó")
            .performScrollTo().performTextInput(descripcionPrueba)
        rule.onNodeWithText("Confirmo que los datos que entregué son correctos")
            .performScrollTo().performClick()
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Ver mis siniestros").performScrollTo().assertExists()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = SiniestroDbHelper(context)
        try {
            assertTrue(db.listar(Asegurado.CAMILA).any { it.descripcion == descripcionPrueba })
        } finally {
            db.close()
        }

        rule.onNodeWithText("Ver mis siniestros").performScrollTo().performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
    }
}
