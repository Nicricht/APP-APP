package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RutasAdicionalesCoberturaTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun fijarAsegurado() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("sesion_demo", 0).edit()
                .putString("asegurado", "CAMILA").commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @After fun restaurar() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", "CAMILA").commit()
    }

    @Test fun avisosNavegaYVuelveALaLista() {
        if (rule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("Cerrar sesión").performClick()
        }
        rule.onNodeWithText("Continuar como Camila (demo)").performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Avisos").performClick()
        rule.onNodeWithText("Avisos de mis siniestros").assertIsDisplayed()
        rule.onNodeWithText("Volver a mis siniestros").performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
    }

    @Test fun detalleEdicionYEvidenciasRestauranLaNavegacion() {
        if (rule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithText("Cerrar sesión").performClick()
        }
        rule.onNodeWithText("Continuar como Camila (demo)").performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()
        rule.onNodeWithText("Estado: Recibido").performClick()
        rule.onNodeWithText("Detalle del siniestro").assertIsDisplayed()
        rule.onNodeWithText("Corregir descripción").performScrollTo().performClick()
        rule.onNodeWithText("Corregir reporte").assertIsDisplayed()
        rule.onNodeWithText("Volver").performClick()
        rule.onNodeWithText("Detalle del siniestro").assertIsDisplayed()
        rule.onNodeWithText("Documentos y evidencias").performScrollTo().performClick()
        rule.onNodeWithText("Evidencias guardadas: 0/15").assertExists()
        rule.onNodeWithText("Volver al siniestro").performClick()
        rule.onNodeWithText("Detalle del siniestro").assertIsDisplayed()
        rule.onNodeWithText("Volver").assertIsDisplayed().performClick()
        rule.waitForIdle()
        rule.waitUntil(8000) {
            rule.onAllNodesWithText("Mis siniestros", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Mis siniestros").assertIsDisplayed()
    }
}
