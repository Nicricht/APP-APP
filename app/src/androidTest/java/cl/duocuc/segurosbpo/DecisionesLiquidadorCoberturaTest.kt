package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
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
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class DecisionesLiquidadorCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro) = Siniestro(
        id = 42,
        numero = "SIN-26-000042",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Maipú",
        descripcion = "Filtración en vivienda",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026 09:00",
        liquidadorAsignado = Liquidador.HOGAR
    )

    @Test fun evaluacionConDocumentoPendienteBloqueaLiquidacion() {
        val cargas = AtomicInteger(0)
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso(EtapaSiniestro.EVALUACION)),
                gestiones = emptyList(),
                solicitudes = listOf(
                    SolicitudDocumento(1, 42, "Constancia", "Subir PDF", "22-10-2026", null)
                ),
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = {},
                onCargarGestiones = { cargas.incrementAndGet() },
                onRegistrarGestion = { _, _, _ -> null },
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = { _, _, _ -> null },
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000042").performScrollTo().performClick()
        rule.onNodeWithText("Faltan 1 documentos. Deben entregarse antes de avanzar.").assertExists()
        rule.onNodeWithText("Confirmar avance a En liquidación").assertIsNotEnabled()
        rule.onNodeWithText("Pendiente").assertExists()
        rule.waitForIdle()
        assertEquals(1, cargas.get())
    }

    @Test fun evaluacionPermiteRegistrarCierreAnticipadoConJustificacion() {
        val cierre = AtomicReference<String>("")
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso(EtapaSiniestro.EVALUACION)),
                gestiones = emptyList(),
                solicitudes = emptyList(),
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = { _, _, _ -> null },
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = { _, resultado, mensaje ->
                    cierre.set(resultado + ":" + mensaje)
                    null
                },
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000042").performScrollTo().performClick()
        rule.onNodeWithText("Rechazado").performScrollTo().performClick()
        rule.onNodeWithText("Mensaje de cierre obligatorio").performScrollTo().performTextInput("Resolución informada")
        rule.onNodeWithText("Confirmar cierre anticipado").performScrollTo().performClick()
        assertEquals("Rechazado:Resolución informada", cierre.get())
    }
}
