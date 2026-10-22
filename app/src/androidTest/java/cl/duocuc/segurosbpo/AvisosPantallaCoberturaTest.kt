package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.AvisoSiniestro
import cl.duocuc.segurosbpo.ui.screens.AvisosScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@RunWith(AndroidJUnit4::class)
class AvisosPantallaCoberturaTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun avisosVaciosMuestranExplicacionYPermitenRegresar() {
        val cargo = AtomicBoolean(false)
        val regreso = AtomicBoolean(false)
        composeRule.setContent {
            AvisosScreen(
                avisos = emptyList(),
                onCargar = { cargo.set(true) },
                onMarcarLeido = {},
                onAbrirCaso = {},
                onVolver = { regreso.set(true) }
            )
        }
        composeRule.onNodeWithText("No tienes avisos nuevos ni anteriores.").assertIsDisplayed()
        composeRule.onNodeWithText("Volver a mis siniestros").performClick()
        composeRule.waitForIdle()
        assertTrue(cargo.get())
        assertTrue(regreso.get())
    }

    @Test fun sePuedeAbrirCasoYMarcarAvisoComoLeido() {
        val leido = AtomicLong(-1)
        val abierto = AtomicLong(-1)
        val aviso = AvisoSiniestro(
            id = 12,
            siniestroId = 34,
            numeroSiniestro = "SIN-26-009999",
            mensaje = "Documento pendiente de entregar",
            fecha = "22-10-2026 14:00",
            leido = false
        )
        composeRule.setContent {
            AvisosScreen(
                avisos = listOf(aviso),
                onCargar = {},
                onMarcarLeido = { leido.set(it) },
                onAbrirCaso = { abierto.set(it) },
                onVolver = {}
            )
        }
        composeRule.onNodeWithText("Documento pendiente de entregar").assertIsDisplayed()
        composeRule.onNodeWithText("Marcar como leído").performClick()
        assertEquals(12L, leido.get())
        composeRule.onNodeWithText("Ver siniestro").performClick()
        assertEquals(34L, abierto.get())
    }

    @Test fun avisoYaLeidoNoOfreceMarcarOtraVez() {
        composeRule.setContent {
            AvisosScreen(
                avisos = listOf(
                    AvisoSiniestro(10, 20, "SIN-26-000020", "Caso en evaluación", "22-10-2026", true)
                ),
                onCargar = {},
                onMarcarLeido = {},
                onAbrirCaso = {},
                onVolver = {}
            )
        }
        composeRule.onNodeWithText("Leído").assertIsDisplayed()
        composeRule.onNodeWithText("Marcar como leído").assertDoesNotExist()
    }
}
