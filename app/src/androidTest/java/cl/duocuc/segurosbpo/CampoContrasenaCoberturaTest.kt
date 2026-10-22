package cl.duocuc.segurosbpo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class CampoContrasenaCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun campoOcultaYMuestraElValorPorAccionDelUsuario() {
        val valorIngresado = AtomicReference("")
        rule.setContent {
            var valor by remember { mutableStateOf("") }
            var visible by remember { mutableStateOf(false) }
            CampoContrasena(
                valor = valor,
                onValorCambia = {
                    valor = it
                    valorIngresado.set(it)
                },
                etiqueta = "Contraseña de ejemplo",
                visible = visible,
                onAlternarVisible = { visible = !visible }
            )
        }
        rule.onNodeWithText("Contraseña de ejemplo").performTextInput("clave123")
        assertEquals("clave123", valorIngresado.get())
        rule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        rule.onNodeWithContentDescription("Ocultar Contraseña").assertExists()
        rule.onNodeWithContentDescription("Ocultar Contraseña").performClick()
        rule.onNodeWithContentDescription("Mostrar contraseña").assertExists()
    }

    @Test fun mensajeDeErrorDelComponenteEsVisible() {
        rule.setContent {
            CampoContrasena(
                valor = "",
                onValorCambia = {},
                etiqueta = "Contraseña de ejemplo",
                visible = false,
                onAlternarVisible = {},
                error = "Longitud insuficiente"
            )
        }
        rule.onNodeWithText("Longitud insuficiente").assertExists()
    }
}
