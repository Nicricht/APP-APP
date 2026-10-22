package cl.duocuc.segurosbpo

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.components.CasillaConTexto
import cl.duocuc.segurosbpo.ui.components.GrupoRadio
import cl.duocuc.segurosbpo.ui.components.InterruptorConTexto
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlesSeleccionCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun casillaEInterruptorAlternanValoresConLaFilaCompleta() {
        rule.setContent {
            var confirmacion by remember { mutableStateOf(false) }
            var avisos by remember { mutableStateOf(true) }
            Column {
                CasillaConTexto(
                    texto = if (confirmacion) "Confirmación activa" else "Confirmación pendiente",
                    marcado = confirmacion,
                    onCambio = { confirmacion = it }
                )
                InterruptorConTexto(
                    texto = if (avisos) "Avisos activados" else "Avisos desactivados",
                    activado = avisos,
                    onCambio = { avisos = it }
                )
            }
        }
        rule.onNodeWithText("Confirmación pendiente").performClick()
        rule.onNodeWithText("Confirmación activa").assertExists()
        rule.onNodeWithText("Avisos activados").performClick()
        rule.onNodeWithText("Avisos desactivados").assertExists()
    }

    @Test fun opcionesRadioActualizanSeleccionYAceptanListaVacia() {
        rule.setContent {
            var seleccion by remember { mutableStateOf("Hogar") }
            Column {
                GrupoRadio(
                    opciones = listOf("Vehículos", "Hogar"),
                    seleccionada = seleccion,
                    onSeleccionar = { seleccion = it },
                    textoDe = { it }
                )
                androidx.compose.material3.Text("Elegido: $seleccion")
                GrupoRadio(
                    opciones = emptyList<String>(),
                    seleccionada = "",
                    onSeleccionar = {},
                    textoDe = { it }
                )
            }
        }
        rule.onNodeWithText("Elegido: Hogar").assertExists()
        rule.onNodeWithText("Vehículos").performClick()
        rule.onNodeWithText("Elegido: Vehículos").assertExists()
        rule.onNodeWithText("Hogar").performClick()
        rule.onNodeWithText("Elegido: Hogar").assertExists()
    }
}
