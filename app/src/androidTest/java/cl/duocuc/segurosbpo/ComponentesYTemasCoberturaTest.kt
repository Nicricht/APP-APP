package cl.duocuc.segurosbpo

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.components.BotonPrincipal
import cl.duocuc.segurosbpo.ui.components.SelectorOpciones
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.ui.theme.admiteColoresDinamicos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class ComponentesYTemasCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun temaClaroSinColoresDinamicos() {
        rule.setContent {
            SegurosBpoTheme(darkTheme = false, dynamicColor = false) {
                Text("Modo claro")
            }
        }
        rule.onNodeWithText("Modo claro").assertExists()
    }

    @Test fun temaOscuroSinColoresDinamicos() {
        rule.setContent {
            SegurosBpoTheme(darkTheme = true, dynamicColor = false) {
                Text("Modo oscuro")
            }
        }
        rule.onNodeWithText("Modo oscuro").assertExists()
    }

    @Test fun temaClaroConColoresDinamicos() {
        rule.setContent {
            SegurosBpoTheme(darkTheme = false, dynamicColor = true) {
                Text("Colores dinámicos claros")
            }
        }
        rule.onNodeWithText("Colores dinámicos claros").assertExists()
    }

    @Test fun temaOscuroConColoresDinamicos() {
        rule.setContent {
            SegurosBpoTheme(darkTheme = true, dynamicColor = true) {
                Text("Colores dinámicos oscuros")
            }
        }
        rule.onNodeWithText("Colores dinámicos oscuros").assertExists()
    }

    @Test fun botonesHabilitadosConYSinIcono() {
        val presionado = AtomicBoolean(false)
        rule.setContent {
            androidx.compose.foundation.layout.Column {
                BotonPrincipal(texto = "Con icono", icono = R.drawable.ic_check, onClick = {
                    presionado.set(true)
                })
                BotonPrincipal(texto = "Deshabilitado", habilitado = false, onClick = {})
                BotonPrincipal(texto = "Sin icono", onClick = {})
            }
        }
        rule.onNodeWithText("Deshabilitado").assertIsNotEnabled()
        rule.onNodeWithText("Con icono").performClick()
        assertTrue(presionado.get())
        rule.onNodeWithText("Sin icono").assertExists()
    }

    @Test fun selectorPermiteCambiarYCancelar() {
        rule.setContent {
            var seleccion by remember { mutableStateOf("Primera") }
            SelectorOpciones(
                titulo = "Opciones de ejemplo",
                opciones = listOf("Primera", "Segunda", "Tercera"),
                seleccionada = seleccion,
                onSeleccionar = { seleccion = it },
                textoDe = { it }
            )
        }
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("Segunda").performClick()
        rule.onNodeWithText("Segunda").assertExists()
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("Cancelar").performClick()
        rule.onNodeWithText("Segunda").assertExists()
    }

    @Test fun versionAndroidDeterminaDisponibilidadDeColoresDinamicos() {
        assertTrue(admiteColoresDinamicos(true, 31))
        assertTrue(admiteColoresDinamicos(true, 35))
        org.junit.Assert.assertFalse(admiteColoresDinamicos(true, 30))
        org.junit.Assert.assertFalse(admiteColoresDinamicos(false, 35))
    }

    @Test fun temaConConfiguracionParcialPermiteUsarValoresPredeterminados() {
        rule.setContent {
            androidx.compose.foundation.layout.Column {
                SegurosBpoTheme(dynamicColor = false) { Text("Solo color no dinámico") }
                SegurosBpoTheme(darkTheme = true) { Text("Solo tema oscuro") }
            }
        }
        rule.onNodeWithText("Solo color no dinámico").assertExists()
        rule.onNodeWithText("Solo tema oscuro").assertExists()
    }
}
