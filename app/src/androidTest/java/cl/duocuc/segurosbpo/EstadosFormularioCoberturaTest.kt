package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.FormularioContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EstadosFormularioCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun mostrar(estado: FormularioUiState) {
        rule.setContent {
            FormularioContent(
                uiState = estado,
                onAseguradoSeleccionado = {},
                onFechaCambia = {},
                onComunaCambia = {},
                onDescripcionCambia = {},
                onCompaniaSeleccionada = {},
                onTipoSeleccionado = {},
                onTercerosCambia = {},
                onDatosCorrectosCambia = {},
                onRegistrar = {},
                onLimpiar = {},
                onVolver = {},
                onVerMisSiniestros = {}
            )
        }
    }

    @Test fun todosLosErroresDelFormularioSonVisibles() {
        mostrar(
            FormularioUiState(
                errorCompania = "La compañía no está disponible",
                errorDescripcion = "Cuéntanos qué pasó",
                errorFecha = "La fecha es futura",
                errorComuna = "Indica la comuna",
                errorDatos = "Confirma los datos"
            )
        )
        rule.onNodeWithText("La compañía no está disponible").assertExists()
        rule.onNodeWithText("Cuéntanos qué pasó").assertExists()
        rule.onNodeWithText("La fecha es futura").assertExists()
        rule.onNodeWithText("Indica la comuna").assertExists()
        rule.onNodeWithText("Confirma los datos").assertExists()
    }

    @Test fun vehiculoConTercerosMuestraAdvertencia() {
        mostrar(
            FormularioUiState(
                tipoSiniestro = TipoSiniestro.CHOQUE,
                huboTerceros = true
            )
        )
        rule.onNodeWithText(
            "Como hubo terceros, más adelante te vamos a pedir la constancia de Carabineros."
        ).assertExists()
    }

    @Test fun reporteMuyAntiguoMuestraAdvertenciaSinBloquear() {
        mostrar(
            FormularioUiState(
                tipoSiniestro = TipoSiniestro.DANO_AGUA,
                fechaOcurrencia = "01-01-2001",
                datosCorrectos = true
            )
        )
        rule.onNodeWithText(
            "Reporte fuera de plazo: pasaron más de 30 días desde el siniestro. Puedes continuar con el reporte."
        ).assertExists()
        rule.onNodeWithText("Reportar").assertExists()
    }

    @Test fun registroExitosoMuestraNumeroYPasoSiguiente() {
        mostrar(
            FormularioUiState(
                asegurado = Asegurado.CAMILA,
                compania = Compania.CORDILLERA,
                reporteExitoso = true,
                numeroSiniestro = "SIN-26-004321"
            )
        )
        rule.waitForIdle()
        rule.onNodeWithText("Ver mis siniestros").performScrollTo().assertExists()
    }
}
