package cl.duocuc.segurosbpo

import androidx.compose.material3.Text
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
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

    @Test fun temaEstablePermiteActualizarUnHermanoSinCambiarLaPaleta() {
        val actualizacion = androidx.compose.runtime.mutableStateOf(0)
        rule.setContent {
            androidx.compose.foundation.layout.Column {
                Text("Actualización: " + actualizacion.value)
                SegurosBpoTheme(darkTheme = false, dynamicColor = false) {
                    Text("Tema estable")
                }
            }
        }
        rule.onNodeWithText("Actualización: 0").assertExists()
        rule.onNodeWithText("Tema estable").assertExists()
        rule.runOnIdle { actualizacion.value = 1 }
        rule.onNodeWithText("Actualización: 1").assertExists()
        rule.runOnIdle { actualizacion.value = 2 }
        rule.onNodeWithText("Actualización: 2").assertExists()
        rule.onNodeWithText("Tema estable").assertExists()
    }

    @Test fun temaPredeterminadoSePuedeRecomponerConCambioDeContenido() {
        val contenido = androidx.compose.runtime.mutableStateOf("Primera pantalla")
        rule.setContent {
            SegurosBpoTheme {
                Text(contenido.value)
            }
        }
        rule.onNodeWithText("Primera pantalla").assertExists()
        rule.runOnIdle { contenido.value = "Segunda pantalla" }
        rule.onNodeWithText("Segunda pantalla").assertExists()
        rule.runOnIdle { contenido.value = "Tercera pantalla" }
        rule.onNodeWithText("Tercera pantalla").assertExists()
    }

    @Test fun temaPredeterminadoRespetaModoOscuroSimuladoEnConfiguracion() {
        rule.setContent {
            val configuracion = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    Configuration.UI_MODE_NIGHT_YES
            }
            CompositionLocalProvider(LocalConfiguration provides configuracion) {
                SegurosBpoTheme(dynamicColor = false) {
                    Text("Tema oscuro del sistema")
                    val fondo = androidx.compose.material3.MaterialTheme.colorScheme.background
                    Text("Fondo oscuro: " + (fondo == cl.duocuc.segurosbpo.ui.theme.FondoOscuro))
                }
            }
        }
        rule.onNodeWithText("Tema oscuro del sistema").assertExists()
        rule.onNodeWithText("Fondo oscuro: true").assertExists()
    }

    @Test fun temaPredeterminadoRespetaModoClaroSimuladoEnConfiguracion() {
        rule.setContent {
            val configuracion = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    Configuration.UI_MODE_NIGHT_NO
            }
            CompositionLocalProvider(LocalConfiguration provides configuracion) {
                SegurosBpoTheme(dynamicColor = false) {
                    Text("Tema claro del sistema")
                    val fondo = androidx.compose.material3.MaterialTheme.colorScheme.background
                    Text("Fondo claro: " + (fondo == cl.duocuc.segurosbpo.ui.theme.FondoClaro))
                }
            }
        }
        rule.onNodeWithText("Tema claro del sistema").assertExists()
        rule.onNodeWithText("Fondo claro: true").assertExists()
    }
}
