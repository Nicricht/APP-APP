package cl.duocuc.segurosbpo

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class ErroresLiquidadorCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro) = Siniestro(
        id = 191,
        numero = "SIN-26-000191",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "22-10-2026",
        comuna = "Maipú",
        descripcion = "Filtración en domicilio",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "22-10-2026 14:00",
        liquidadorAsignado = Liquidador.HOGAR
    )

    private fun mostrar(
        etapa: EtapaSiniestro,
        onDocumento: (Long, String, String) -> String? = { _, _, _ -> null },
        onAvanzar: (Long, String, String) -> String? = { _, _, _ -> null },
        onCierre: (Long, String, String) -> String? = { _, _, _ -> null }
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
                onRegistrarGestion = { _, _, _ -> null },
                onAvanzar = onAvanzar,
                onCerrarAnticipado = onCierre,
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000191").performScrollTo().performClick()
    }

    private fun verificarAviso(mensaje: String) {
        rule.waitUntil(10000) {
            rule.onAllNodesWithText(mensaje).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun avanceRechazadoConservaLaEtapa() {
        val recibido = AtomicReference(-1L)
        mostrar(
            etapa = EtapaSiniestro.RECIBIDO,
            onAvanzar = { id, _, _ ->
                recibido.set(id)
                "La etapa no se pudo actualizar"
            }
        )
        rule.onNodeWithText("Confirmar avance a En evaluación").performScrollTo().performClick()
        verificarAviso("La etapa no se pudo actualizar")
        assertEquals(191L, recibido.get())
        rule.onNodeWithText("Etapa actual: Recibido").assertExists()
    }

    @Test fun solicitudRechazadaNoSeRegistraComoExitosa() {
        val recibido = AtomicReference("")
        mostrar(
            etapa = EtapaSiniestro.EVALUACION,
            onDocumento = { id, nombre, _ ->
                recibido.set("$id:$nombre")
                "El documento ya está pendiente"
            }
        )
        rule.onNodeWithText("Nombre del documento requerido")
            .performScrollTo().performTextInput("Boleta")
        rule.onNode(hasText("Solicitar documento") and hasClickAction())
            .performScrollTo().performClick()
        verificarAviso("El documento ya está pendiente")
        assertEquals("191:Boleta", recibido.get())
    }

    @Test fun cierreRechazadoNoCambiaElEstadoVisible() {
        val recibido = AtomicReference("")
        mostrar(
            etapa = EtapaSiniestro.RECIBIDO,
            onCierre = { id, resultado, _ ->
                recibido.set("$id:$resultado")
                "No se pudo cerrar el siniestro"
            }
        )
        rule.onNodeWithText("Desistido").performScrollTo().performClick()
        rule.onNodeWithText("Mensaje de cierre obligatorio")
            .performScrollTo().performTextInput("Solicitud confirmada")
        rule.onNodeWithText("Confirmar cierre anticipado").performScrollTo().performClick()
        verificarAviso("No se pudo cerrar el siniestro")
        assertEquals("191:Desistido", recibido.get())
        rule.onNodeWithText("Etapa actual: Recibido").assertExists()
    }
}
