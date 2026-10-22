package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class AccionesLiquidadorCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro) = Siniestro(
        id = 62,
        numero = "SIN-26-000062",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Maipú",
        descripcion = "Filtración de agua",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026",
        liquidadorAsignado = Liquidador.HOGAR
    )

    @Test fun guardaGestionVisibleYSolicitaDocumento() {
        val gestion = AtomicReference<String?>(null)
        val solicitud = AtomicReference<String?>(null)
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso(EtapaSiniestro.EVALUACION)),
                gestiones = emptyList(),
                solicitudes = emptyList(),
                onSolicitarDocumento = { id, tipo, comentario ->
                    solicitud.set("$id:$tipo:$comentario")
                    null
                },
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = { id, descripcion, visible ->
                    gestion.set("$id:$descripcion:$visible")
                    null
                },
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = { _, _, _ -> null },
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000062").performScrollTo().performClick()
        rule.onNodeWithText("Descripción de gestión").performTextInput("Gestión revisada")
        rule.onNodeWithText("Registrar gestión").performScrollTo().performClick()
        assertEquals("62:Gestión revisada:true", gestion.get())
        rule.onNodeWithText("Nombre del documento requerido").performTextInput("Boleta de reparación")
        rule.onNodeWithText("Indicaciones para el asegurado (opcional)").performTextInput("Presentar imagen")
        rule.onNode(hasText("Solicitar documento") and hasClickAction()).performScrollTo().performClick()
        assertEquals("62:Boleta de reparación:Presentar imagen", solicitud.get())
    }

    @Test fun cierraCasoDesdeLiquidacion() {
        val resultado = AtomicReference<String?>(null)
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso(EtapaSiniestro.LIQUIDACION)),
                gestiones = emptyList(),
                solicitudes = emptyList(),
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = { _, _, _ -> null },
                onAvanzar = { id, estado, comentario ->
                    resultado.set("$id:$estado:$comentario")
                    null
                },
                onCerrarAnticipado = { _, _, _ -> null },
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000062").performScrollTo().performClick()
        rule.onNodeWithText("Aprobado").performScrollTo().performClick()
        rule.onNodeWithText("Mensaje de cierre obligatorio").performScrollTo().performTextInput("Resolución registrada")
        rule.onNodeWithText("Confirmar avance a Cerrado").performScrollTo().performClick()
        assertEquals("62:Aprobado:Resolución registrada", resultado.get())
    }
}
