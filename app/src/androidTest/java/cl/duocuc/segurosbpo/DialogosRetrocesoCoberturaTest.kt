package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.ui.components.SelectorOpciones
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class DialogosRetrocesoCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun retrocesoAndroidDescartaListaDeAseguradosSinCambiarPerfil() {
        val cambios = AtomicInteger(0)
        rule.setContent {
            MisSiniestrosScreen(
                asegurado = Asegurado.CAMILA,
                siniestros = emptyList(),
                busqueda = "",
                avisosNoLeidos = 0,
                onVerAvisos = {},
                onBuscar = {},
                onCambiarAsegurado = { cambios.incrementAndGet() },
                onReportar = {},
                onGestionar = {},
                onVerDetalle = {}
            )
        }
        rule.onNodeWithText("Cambiar asegurado").performClick()
        rule.onNodeWithText("Selecciona un asegurado ficticio").assertExists()
        Espresso.pressBack()
        rule.onNodeWithText("Selecciona un asegurado ficticio").assertDoesNotExist()
        rule.onNodeWithText("Asegurado: ${Asegurado.CAMILA.etiqueta}").assertExists()
        assertEquals(0, cambios.get())
    }

    @Test fun retrocesoAndroidCancelaSelectorSinAlterarDatoElegido() {
        val cambios = AtomicInteger(0)
        rule.setContent {
            SelectorOpciones(
                titulo = "Seleccionar compañía",
                opciones = listOf("Cordillera", "Litoral"),
                seleccionada = "Cordillera",
                onSeleccionar = { cambios.incrementAndGet() },
                textoDe = { it }
            )
        }
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("Cancelar").assertExists()
        Espresso.pressBack()
        rule.onNodeWithText("Cancelar").assertDoesNotExist()
        rule.onNodeWithText("Cordillera").assertExists()
        assertEquals(0, cambios.get())
    }
}
