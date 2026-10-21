package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas del recorrido real en Android con Compose UI Test.
 * No son Playwright: la app es nativa Android, no una página web.
 */
@RunWith(AndroidJUnit4::class)
class NavegacionE2ETest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun deMisSiniestrosAlFormularioYDeVuelta() {
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Reportar").performClick()
        composeRule.onNodeWithText("Volver a mis siniestros").performClick()
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
    }

    @Test
    fun abrePerfilLiquidadorYRegresa() {
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Liquidador").performClick()
        composeRule.onNodeWithText("Gestión de siniestros").assertIsDisplayed()
        composeRule.onNodeWithText("Volver a los asegurados").performClick()
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
    }
}
