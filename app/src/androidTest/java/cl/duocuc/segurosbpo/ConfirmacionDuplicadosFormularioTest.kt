package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class ConfirmacionDuplicadosFormularioTest {
    @get:Rule val rule = createComposeRule()

    private fun formularioValido(): FormularioViewModel = FormularioViewModel().also { vm ->
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Santiago")
        vm.onDescripcionCambia("Otro siniestro ficticio con la misma fecha")
        vm.onDatosCorrectosCambia(true)
    }

    private fun montar(vm: FormularioViewModel, registros: AtomicInteger) {
        rule.setContent {
            FormularioScreen(
                aseguradoActual = Asegurado.CAMILA,
                onRegistroExitoso = {
                    registros.incrementAndGet()
                    "SIN-26-009991"
                },
                onVolver = {},
                onVerMisSiniestros = {},
                viewModel = vm,
                onPosibleDuplicado = { _: FormularioUiState -> "SIN-26-004861" }
            )
        }
    }

    @Test fun debeAceptarExplicitamenteUnDuplicadoAntesDeRegistrar() {
        val registros = AtomicInteger()
        val vm = formularioValido()
        montar(vm, registros)
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Posible siniestro duplicado").assertExists()
        rule.onNodeWithText(
            "Ya existe un caso abierto: SIN-26-004861. Si se trata de otro incidente, puedes continuar."
        ).assertExists()
        assertEquals(0, registros.get())

        rule.onNodeWithText("Revisar datos").performClick()
        rule.onNodeWithText("Posible siniestro duplicado").assertDoesNotExist()
        assertEquals(0, registros.get())

        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Continuar de todos modos").performClick()
        assertEquals(1, registros.get())
        rule.onNodeWithText(
            "Listo. Tu siniestro quedó registrado con el número SIN-26-009991"
        ).assertExists()
        rule.onNodeWithText("Posible siniestro duplicado").assertDoesNotExist()
    }

    @Test fun cancelarLimpiarYReabrirUnDuplicadoNuncaRegistraSinConfirmar() {
        val registros = AtomicInteger()
        val vm = formularioValido()
        montar(vm, registros)
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Posible siniestro duplicado").assertExists()
        rule.onNodeWithText("Revisar datos").performClick()
        rule.onNodeWithText("Posible siniestro duplicado").assertDoesNotExist()
        assertEquals(0, registros.get())

        rule.onNodeWithText("Limpiar").performScrollTo().performClick()
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Posible siniestro duplicado").assertDoesNotExist()
        assertEquals(0, registros.get())

        rule.runOnIdle {
            vm.onFechaCambia("20-10-2026")
            vm.onComunaCambia("Santiago")
            vm.onDescripcionCambia("Otro siniestro ficticio con la misma fecha")
            vm.onDatosCorrectosCambia(true)
        }
        rule.onNodeWithText("Reportar").performScrollTo().performClick()
        rule.onNodeWithText("Posible siniestro duplicado").assertExists()
        assertEquals(0, registros.get())
        rule.onNodeWithText("Revisar datos").performClick()
        assertEquals(0, registros.get())
    }
}
