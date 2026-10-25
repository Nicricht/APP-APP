package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

    @Test
    fun perfilAseguradoAbiertoDesdePantallaPrincipalVuelveALosSiniestros() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Continuar como Camila (demo)").performClick()
        composeRule.onNodeWithText("Mi perfil").performClick()
        composeRule.onNodeWithText("Perfil ficticio de demostración. Los datos no son editables.")
            .assertExists()
        composeRule.onNodeWithText("Volver").performScrollTo().performClick()
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Asegurado: Camila Rojas Fuentes").assertExists()
    }

    @Test
    fun botonEstadisticasYSuRetrocesoConservanLaSesionDelAsegurado() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Continuar como Camila (demo)").performClick()
        composeRule.onNodeWithText("Mis estadísticas").performClick()
        composeRule.onNodeWithText("Estadísticas de mis siniestros").assertExists()
        composeRule.onNodeWithText("Volver a mis siniestros").performScrollTo().performClick()
        composeRule.onNodeWithText("Seguimiento de seguros").assertIsDisplayed()
        composeRule.onNodeWithText("Asegurado: Camila Rojas Fuentes").assertExists()
    }

    @Test
    fun perfilLiquidadorAbiertoDesdeSuPanelRegresaSinCambiarEquipo() {
        if (composeRule.onAllNodesWithText("Cerrar sesión").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Cerrar sesión").performClick()
        }
        composeRule.onNodeWithText("Liquidador de Hogar (demo)").performClick()
        composeRule.onNodeWithText("Mi perfil").performScrollTo().performClick()
        composeRule.onNodeWithText("Equipo: HOGAR").assertExists()
        composeRule.onNodeWithText("Volver").performScrollTo().performClick()
        composeRule.onNodeWithText("Gestión de siniestros").assertIsDisplayed()
        composeRule.onNodeWithText("Responsable: Pedro C.").assertExists()
    }
}
