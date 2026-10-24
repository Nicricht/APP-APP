package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuLateralRolesCoberturaTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun iniciarSinCuentaAnterior() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("cuenta_sesion_local", 0)
                .edit().remove("cuenta_id").commit()
        }
    }

    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @After fun limpiarSesion() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("cuenta_sesion_local", 0)
            .edit().remove("cuenta_id").commit()
    }

    private fun abrirMenu() {
        rule.onNodeWithText("Abrir menú").performClick()
    }

    private fun salirSiHaySesion() {
        if (rule.onAllNodesWithText("Abrir menú").fetchSemanticsNodes().isNotEmpty()) {
            abrirMenu()
            rule.onNodeWithText("Salir de la cuenta").performClick()
        }
    }

    @Test fun aseguradoPuedeNavegarCadaOpcionDeSuMenuSinCambiarRol() {
        salirSiHaySesion()
        rule.onNodeWithText("Continuar como Camila (demo)").performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()

        abrirMenu()
        rule.onNodeWithText("Reportar siniestro").performClick()
        rule.onNodeWithText("Volver a mis siniestros").assertExists()

        abrirMenu()
        rule.onNodeWithText("Ver estadísticas").performClick()
        rule.onNodeWithText("Mis estadísticas").assertExists()

        abrirMenu()
        rule.onNodeWithText("Centro de avisos").performClick()
        rule.onNodeWithText("Avisos de mis siniestros").assertExists()

        abrirMenu()
        rule.onNodeWithText("Cuenta y perfil").performClick()
        rule.onNodeWithText("Mi perfil").assertExists()

        abrirMenu()
        rule.onNodeWithText("Panel del asegurado").performClick()
        rule.onNodeWithText("Seguimiento de seguros").assertExists()

        abrirMenu()
        rule.onNodeWithText("Salir de la cuenta").performClick()
        rule.onNodeWithText("Correo electrónico").assertExists()
    }

    @Test fun liquidadorTieneMenuSeparadoYPuedeCerrarSesion() {
        salirSiHaySesion()
        rule.onNodeWithText("Liquidador de Vehículos (demo)").performClick()
        rule.onNodeWithText("Gestión de siniestros").assertExists()

        abrirMenu()
        rule.onNodeWithText("Cuenta y perfil").performClick()
        rule.onNodeWithText("Mi perfil").assertExists()

        abrirMenu()
        rule.onNodeWithText("Panel de trabajo").performClick()
        rule.onNodeWithText("Gestión de siniestros").assertExists()

        abrirMenu()
        rule.onNodeWithText("Salir de la cuenta").performClick()
        rule.onNodeWithText("Correo electrónico").assertExists()
    }
}
