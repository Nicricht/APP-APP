package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NavegacionE2ETest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun deMisSiniestrosAlFormularioYDeVuelta() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Continuar como Camila (demo)").performClick()

        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Reportar").performClick()
        composeRule.onNodeWithText("Volver a mis siniestros").performClick()
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
    }

    @Test
    fun abrePerfilLiquidadorYRegresa() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Liquidador de Vehículos (demo)").performClick()

        composeRule.onNodeWithText("Gestión de siniestros").assertIsDisplayed()
        composeRule.onNodeWithText("Cerrar sesión").performClick()
        composeRule.onNodeWithText("Correo electrónico").assertIsDisplayed()
    }
}
