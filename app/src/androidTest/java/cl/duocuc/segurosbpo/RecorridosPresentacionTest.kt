package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecorridosPresentacionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun formularioVacioMuestraErroresEnPantalla() {
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Reportar").performClick()
        composeRule.onNodeWithText("Reportar un siniestro").assertIsDisplayed()
        composeRule.onNodeWithText("Reportar").performScrollTo().performClick()
        composeRule.onNodeWithText("Escribe una fecha válida (día-mes-año)").assertExists()
        composeRule.onNodeWithText("Debes confirmar que los datos son correctos").assertExists()
    }

    @Test
    fun sePuedeConsultarHistorialDesdeUnSiniestro() {
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Estado: Recibido").performClick()
        composeRule.onNodeWithText("Detalle del siniestro").assertIsDisplayed()
        composeRule.onNodeWithText("Ver historial").performScrollTo().performClick()
        composeRule.onNodeWithText("Historial del siniestro").assertIsDisplayed()
        composeRule.onNodeWithText("Volver al detalle").performClick()
        composeRule.onNodeWithText("Detalle del siniestro").assertIsDisplayed()
    }

    @Test
    fun evidenciaExigeTipoDeDocumentoAntesDeAbrirGaleria() {
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Estado: Recibido").performClick()
        composeRule.onNodeWithText("Documentos y evidencias").performScrollTo().performClick()
        composeRule.onNodeWithText("Evidencias guardadas: 0/15").assertExists()
        composeRule.onNodeWithText("Elegir de la galería").performScrollTo().performClick()
        composeRule.onNodeWithText("Indica el tipo de documento").assertExists()
    }
}
