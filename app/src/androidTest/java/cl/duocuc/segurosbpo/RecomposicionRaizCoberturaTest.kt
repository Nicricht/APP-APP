package cl.duocuc.segurosbpo

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class RecomposicionRaizCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun parametrosEstablesDelFormularioRecomponenElPadreSinRecrearSuModelo() {
        val actualizacion = mutableStateOf(0)
        val modelo = FormularioViewModel()
        val regresos = AtomicInteger()
        val guardar: (FormularioUiState) -> String = { "SIN-26-001234" }
        val volver: () -> Unit = { regresos.incrementAndGet() }
        val contador = AtomicInteger()

        rule.setContent {
            val version = actualizacion.value
            SideEffect { contador.incrementAndGet() }
            FormularioScreen(
                aseguradoActual = Asegurado.CAMILA,
                onRegistroExitoso = guardar,
                onVolver = volver,
                onVerMisSiniestros = volver,
                viewModel = modelo
            )
            Text("Versión del formulario: " + version)
        }

        rule.onNodeWithText("Versión del formulario: 0").assertExists()
        rule.runOnIdle { actualizacion.value = 1 }
        rule.onNodeWithText("Versión del formulario: 1").assertExists()
        rule.runOnIdle { actualizacion.value = 2 }
        rule.onNodeWithText("Versión del formulario: 2").assertExists()
        rule.waitForIdle()
        assertEquals(Asegurado.CAMILA, modelo.uiState.value.asegurado)
        assertEquals(0, regresos.get())
        assertEquals(true, contador.get() >= 3)
    }

    @Test fun temaConContenidoEstableRespondeARecomposicionesDeRaiz() {
        val actualizacion = mutableStateOf(0)
        val apariciones = AtomicInteger()
        val contenido: @Composable () -> Unit = { Text("Contenido estable del tema") }

        rule.setContent {
            val version = actualizacion.value
            SideEffect { apariciones.incrementAndGet() }
            SegurosBpoTheme(
                darkTheme = false,
                dynamicColor = false,
                content = contenido
            )
            Text("Versión del tema: " + version)
        }

        rule.onNodeWithText("Contenido estable del tema").assertExists()
        rule.runOnIdle { actualizacion.value = 1 }
        rule.onNodeWithText("Versión del tema: 1").assertExists()
        rule.runOnIdle { actualizacion.value = 2 }
        rule.onNodeWithText("Versión del tema: 2").assertExists()
        rule.waitForIdle()
        assertEquals(true, apariciones.get() >= 3)
    }

    @Test fun temaPredeterminadoYExplicitoConservanLaConfiguracionAlRecomponer() {
        val actualizacion = mutableStateOf(0)
        val contenido: @Composable () -> Unit = { Text("Tema activo") }
        rule.setContent {
            val version = actualizacion.value
            SegurosBpoTheme(content = contenido)
            SegurosBpoTheme(darkTheme = true, content = contenido)
            SegurosBpoTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme(), dynamicColor = false, content = contenido)
            Text("Composición: " + version)
        }
        rule.onNodeWithText("Composición: 0").assertExists()
        rule.runOnIdle { actualizacion.value = 1 }
        rule.onNodeWithText("Composición: 1").assertExists()
        rule.runOnIdle { actualizacion.value = 2 }
        rule.onNodeWithText("Composición: 2").assertExists()
    }
}
