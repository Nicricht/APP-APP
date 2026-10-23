package cl.duocuc.segurosbpo

import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class FormulariosLiquidadorLimitesTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro) = Siniestro(
        id = 298,
        numero = "SIN-26-000298",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "22-10-2026",
        comuna = "Santiago",
        descripcion = "Prueba de documentos y gestiones",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "22-10-2026 20:30",
        liquidadorAsignado = Liquidador.HOGAR
    )

    private fun abrir(
        etapa: EtapaSiniestro,
        onGestion: (Long, String, Boolean) -> String? = { _, _, _ -> null },
        onDocumento: (Long, String, String) -> String? = { _, _, _ -> null },
        onAvanzar: (Long, String, String) -> String? = { _, _, _ -> null },
        onCerrar: (Long, String, String) -> String? = { _, _, _ -> null }
    ) {
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso(etapa)),
                gestiones = emptyList(),
                solicitudes = emptyList(),
                onSolicitarDocumento = onDocumento,
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = onGestion,
                onAvanzar = onAvanzar,
                onCerrarAnticipado = onCerrar,
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000298").performScrollTo().performClick()
    }

    @Test fun gestionInternaConDescripcionValidaSeGuardaSinVisibilidadPublica() {
        val valores = AtomicReference("")
        abrir(
            EtapaSiniestro.EVALUACION,
            onGestion = { id, descripcion, visible ->
                valores.set("$id:$descripcion:$visible")
                null
            }
        )
        rule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On)).performScrollTo().performClick()
        rule.onNodeWithText("Gestión interna").assertExists()
        rule.onNodeWithText("Descripción de gestión")
            .performScrollTo().performTextInput("Revisión privada del documento")
        rule.onNodeWithText("Registrar gestión").performScrollTo().performClick()
        assertEquals("298:Revisión privada del documento:false", valores.get())
    }

    @Test fun gestionConQuinientosUnCaracteresNoPuedeGuardarse() {
        abrir(EtapaSiniestro.EVALUACION)
        rule.onNodeWithText("Registrar gestión").assertIsNotEnabled()
        rule.onNodeWithText("Descripción de gestión")
            .performScrollTo().performTextInput("A".repeat(501))
        rule.onNodeWithText("Registrar gestión").assertIsNotEnabled()
        rule.onNodeWithText("Obligatorio, hasta 500 caracteres").assertExists()
    }

    @Test fun solicitudConNombreDemasiadoLargoQuedaDeshabilitada() {
        abrir(EtapaSiniestro.RECIBIDO)
        rule.onNodeWithText("Nombre del documento requerido")
            .performScrollTo().performTextInput("B".repeat(101))
        rule.onNode(hasText("Solicitar documento") and hasClickAction()).assertExists()
        rule.onNode(hasText("Solicitar documento") and hasClickAction())
            .assertIsNotEnabled()
    }

    @Test fun solicitudConIndicacionesMuyLargasNoSeEnvía() {
        abrir(EtapaSiniestro.EVALUACION)
        rule.onNodeWithText("Nombre del documento requerido")
            .performScrollTo().performTextInput("Boleta")
        rule.onNodeWithText("Indicaciones para el asegurado (opcional)")
            .performScrollTo().performTextInput("C".repeat(501))
        rule.onNode(hasText("Solicitar documento") and hasClickAction())
            .assertIsNotEnabled()
    }

    @Test fun rechazoDefinitivoEnLiquidacionSeGuardaConMensaje() {
        val valores = AtomicReference("")
        abrir(
            EtapaSiniestro.LIQUIDACION,
            onAvanzar = { id, resultado, texto ->
                valores.set("$id:$resultado:$texto")
                null
            }
        )
        rule.onNodeWithText("Rechazado").performScrollTo().performClick()
        rule.onNodeWithText("Mensaje de cierre obligatorio")
            .performScrollTo().performTextInput("Resolución negativa informada")
        rule.onNodeWithText("Confirmar avance a Cerrado").performScrollTo().performClick()
        assertEquals("298:Rechazado:Resolución negativa informada", valores.get())
    }

    @Test fun desistimientoEnEvaluacionEsUnaOpcionPermitida() {
        val resultado = AtomicReference("")
        abrir(
            EtapaSiniestro.EVALUACION,
            onCerrar = { id, tipo, texto ->
                resultado.set("$id:$tipo:$texto")
                null
            }
        )
        rule.onNodeWithText("Desistido").performScrollTo().performClick()
        rule.onNodeWithText("Mensaje de cierre obligatorio")
            .performScrollTo().performTextInput("Renuncia expresada por asegurado")
        rule.onNodeWithText("Confirmar cierre anticipado").performScrollTo().performClick()
        assertTrue(resultado.get().contains("298:Desistido:Renuncia expresada"))
    }
}
