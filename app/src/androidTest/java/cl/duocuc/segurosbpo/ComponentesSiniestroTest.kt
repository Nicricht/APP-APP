package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentesSiniestroTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun seMuestranAvatarYEstadoDelAsegurado() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Continuar como Camila (demo)").performClick()
        composeRule.onNodeWithContentDescription("Avatar ilustrativo del asegurado").assertExists()
        composeRule.onNodeWithText("Estado: Recibido").assertExists()
    }

    @Test
    fun liquidadorMuestraTarjetasEstadisticas() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Liquidador de Vehículos (demo)").performClick()
        composeRule.onNodeWithText("Total").assertExists()
        composeRule.onNodeWithText("Abiertos").assertExists()
        composeRule.onNodeWithText("Cerrados").assertExists()
    }
}
