package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RegistroPantallaCompletoTest {
    @get:Rule val rule = createComposeRule()

    @Test fun registroValidoConfirmaNumeroYPermiteVolver() {
        val vm = FormularioViewModel()
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Santiago")
        vm.onDescripcionCambia("Daño en vehículo reportado")
        vm.onDatosCorrectosCambia(true)
        val llamadas = AtomicInteger(0)
        val regreso = AtomicBoolean(false)
        rule.setContent {
            FormularioScreen(
                aseguradoActual = Asegurado.CAMILA,
                onRegistroExitoso = {
                    llamadas.incrementAndGet()
                    "SIN-26-090001"
                },
                onVolver = {},
                onVerMisSiniestros = { regreso.set(true) },
                viewModel = vm
            )
        }
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.waitForIdle()
        assertEquals(1, llamadas.get())
        assertEquals("SIN-26-090001", vm.uiState.value.numeroSiniestro)
        assertTrue(vm.uiState.value.reporteExitoso)
        rule.onNodeWithText("Ver mis siniestros").performScrollTo().performClick()
        assertTrue(regreso.get())
    }

    @Test fun registroVacioNoNotificaExito() {
        val vm = FormularioViewModel()
        val llamadas = AtomicInteger(0)
        rule.setContent {
            FormularioScreen(
                aseguradoActual = Asegurado.CAMILA,
                onRegistroExitoso = {
                    llamadas.incrementAndGet()
                    "SIN-26-090002"
                },
                onVolver = {},
                onVerMisSiniestros = {},
                viewModel = vm
            )
        }
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.waitForIdle()
        assertEquals(0, llamadas.get())
        assertFalse(vm.uiState.value.reporteExitoso)
        rule.onNodeWithText("Debes confirmar que los datos son correctos").assertExists()
    }

    @Test fun reporteSinDetectorDeDuplicadosConfiguradoConfirmaYPermiteRegresar() {
        val llamadas = AtomicInteger(0)
        val regreso = AtomicBoolean(false)
        val fechaAislada = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).format(
            Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60))
        )
        rule.setContent {
            FormularioScreen(
                aseguradoActual = Asegurado.CAMILA,
                onRegistroExitoso = {
                    llamadas.incrementAndGet()
                    "SIN-26-090003"
                },
                onVolver = {},
                onVerMisSiniestros = { regreso.set(true) }
            )
        }
        rule.onNodeWithText("Fecha en que ocurrió (día-mes-año)").performScrollTo()
            .performTextInput(fechaAislada)
        rule.onNodeWithText("Comuna donde ocurrió").performScrollTo()
            .performTextInput("Santiago")
        rule.onNodeWithText("Cuéntanos brevemente qué pasó").performScrollTo()
            .performTextInput("Reporte de prueba sin buscar duplicados")
        rule.onNodeWithText("Confirmo que los datos que entregué son correctos")
            .performScrollTo().performClick()
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Ver mis siniestros").performScrollTo().assertExists()
        assertEquals(1, llamadas.get())
        rule.onNodeWithText("Ver mis siniestros").performScrollTo().performClick()
        assertTrue(regreso.get())
    }
}
