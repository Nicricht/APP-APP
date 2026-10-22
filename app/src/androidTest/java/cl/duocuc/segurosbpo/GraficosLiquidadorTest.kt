package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GraficosLiquidadorTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun graficosSonVisiblesSinPulsarUnBotonAdicional() {
        composeRule.onNodeWithText("Liquidador").performClick()
        composeRule.onNodeWithText("Gestión de siniestros").assertIsDisplayed()
        composeRule.onNodeWithText("Resumen del equipo Vehículos").assertIsDisplayed()
        composeRule.onNodeWithText("Casos por etapa").assertExists()
        composeRule.onNodeWithText("Abiertos y cerrados").assertExists()
    }

    @Test
    fun graficosCambianAlSeleccionarHogar() {
        composeRule.onNodeWithText("Liquidador").performClick()
        composeRule.onNodeWithText("Hogar").performClick()
        composeRule.onNodeWithText("Resumen del equipo Hogar").assertIsDisplayed()
    }
}
