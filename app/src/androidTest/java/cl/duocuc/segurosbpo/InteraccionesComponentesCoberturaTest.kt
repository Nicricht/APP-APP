package cl.duocuc.segurosbpo

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.components.SelectorOpciones
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class InteraccionesComponentesCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun selectorDestacaOpcionElegidaYActualizaLaSeleccion() {
        val seleccionado = AtomicReference("Primera")
        rule.setContent {
            var opcion by remember { mutableStateOf("Primera") }
            SelectorOpciones(
                titulo = "Tipo de documento",
                opciones = listOf("Primera", "Segunda"),
                seleccionada = opcion,
                onSeleccionar = {
                    seleccionado.set(it)
                    opcion = it
                },
                textoDe = { it }
            )
        }
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("✓  Primera").assertExists()
        rule.onNodeWithText("Segunda").performClick()
        assertEquals("Segunda", seleccionado.get())
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("✓  Segunda").assertExists()
        rule.onNodeWithText("Cancelar").performClick()
        rule.onNodeWithText("✓  Segunda").assertDoesNotExist()
    }

    @Test fun selectorSinOpcionesPermiteCerrarElDialogo() {
        rule.setContent {
            SelectorOpciones(
                titulo = "Ningún valor disponible",
                opciones = emptyList<String>(),
                seleccionada = "Sin documentos",
                onSeleccionar = {},
                textoDe = { it }
            )
        }
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("Cancelar").performClick()
        rule.onNodeWithText("Sin documentos").assertExists()
    }

    @Test fun campoMultilineaPermiteIngresarDescripcionYCorregirErrores() {
        val registrado = AtomicReference("")
        rule.setContent {
            var texto by remember { mutableStateOf("") }
            Column {
                CampoTexto(
                    valor = texto, onValorCambia = {
                        registrado.set(it)
                        texto = it
                    },
                    etiqueta = "Detalle de avería",
                    icono = R.drawable.ic_correo,
                    error = if (texto.isBlank()) "Descripción requerida" else null,
                    multilinea = true
                )
            }
        }
        rule.onNodeWithText("Descripción requerida").assertExists()
        rule.onNodeWithText("Detalle de avería").performTextInput("Daño documentado en vehículo")
        rule.onNodeWithText("Descripción requerida").assertDoesNotExist()
        assertEquals("Daño documentado en vehículo", registrado.get())
    }

    @Test fun alternarVisibilidadDeContrasenaMantieneLosCaracteresIngresados() {
        val clave = AtomicReference("")
        rule.setContent {
            var valor by remember { mutableStateOf("") }
            var visible by remember { mutableStateOf(false) }
            CampoContrasena(
                valor = valor,
                onValorCambia = {
                    valor = it
                    clave.set(it)
                },
                etiqueta = "Clave de acceso",
                visible = visible,
                onAlternarVisible = { visible = !visible },
                error = if (valor.isBlank()) "Ingrese su clave" else null
            )
        }
        rule.onNodeWithText("Ingrese su clave").assertExists()
        rule.onNodeWithText("Clave de acceso").performTextInput("seguro2026")
        rule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        rule.onNodeWithContentDescription("Ocultar Contraseña").assertExists()
        rule.onNodeWithContentDescription("Ocultar Contraseña").performClick()
        rule.onNodeWithContentDescription("Mostrar contraseña").assertExists()
        assertEquals("seguro2026", clave.get())
    }
}
