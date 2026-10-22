package cl.duocuc.segurosbpo

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
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class RespuestasLiquidadorCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro) = Siniestro(
        id = 83,
        numero = "SIN-26-000083",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "22-10-2026",
        comuna = "Maipú",
        descripcion = "Prueba del liquidador",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "22-10-2026 14:00",
        liquidadorAsignado = Liquidador.HOGAR
    )

    private fun mostrar(
        casos: List<Siniestro>,
        gestiones: List<Gestion> = emptyList(),
        solicitudes: List<SolicitudDocumento> = emptyList(),
        onSeleccionar: (Liquidador) -> Unit = {},
        onGestion: (Long, String, Boolean) -> String? = { _, _, _ -> null },
        onCierre: (Long, String, String) -> String? = { _, _, _ -> null }
    ) {
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = casos,
                gestiones = gestiones,
                solicitudes = solicitudes,
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = onSeleccionar,
                onCargarGestiones = {},
                onRegistrarGestion = onGestion,
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = onCierre,
                onVolver = {}
            )
        }
    }

    @Test fun casoRecibidoPermiteDesistimientoDocumentado() {
        val cierre = AtomicReference("")
        mostrar(
            casos = listOf(caso(EtapaSiniestro.RECIBIDO)),
            onCierre = { id, resultado, mensaje ->
                cierre.set("$id:$resultado:$mensaje")
                null
            }
        )
        rule.onNodeWithText("SIN-26-000083").performScrollTo().performClick()
        rule.onNodeWithText("Desistido").performScrollTo().performClick()
        rule.onNodeWithText("Mensaje de cierre obligatorio")
            .performScrollTo().performTextInput("Desistimiento registrado")
        rule.onNodeWithText("Confirmar cierre anticipado").performScrollTo().performClick()
        assertEquals("83:Desistido:Desistimiento registrado", cierre.get())
    }

    @Test fun errorDeGestionSeMuestraSinPerderContenido() {
        val recibido = AtomicReference("")
        mostrar(
            casos = listOf(caso(EtapaSiniestro.EVALUACION)),
            onGestion = { _, descripcion, _ ->
                recibido.set(descripcion)
                "No se pudo registrar la gestión"
            }
        )
        rule.onNodeWithText("SIN-26-000083").performScrollTo().performClick()
        rule.onNodeWithText("Descripción de gestión")
            .performTextInput("Nota sobre la evaluación")
        rule.onNodeWithText("Registrar gestión").performScrollTo().performClick()
        rule.waitUntil(10000) {
            rule.onAllNodesWithText("No se pudo registrar la gestión")
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals("Nota sobre la evaluación", recibido.get())
    }

    @Test fun casoCerradoMuestraResultadoYEvidenciaDelHistorial() {
        val cerrado = caso(EtapaSiniestro.CERRADO).copy(
            resultadoCierre = "Rechazado",
            mensajeCierre = "Resolución definitiva"
        )
        mostrar(
            casos = listOf(cerrado),
            gestiones = listOf(
                Gestion(3, 83, "22-10-2026 16:00", "Pedro C.", "Informe interno", false),
                Gestion(4, 83, "22-10-2026 16:30", "Pedro C.", "Resultado comunicado", true)
            ),
            solicitudes = listOf(
                SolicitudDocumento(1, 83, "Informe", "Entregado", "21-10-2026", "22-10-2026"),
                SolicitudDocumento(2, 83, "Fotografía", "Pendiente", "22-10-2026", null)
            )
        )
        rule.onNodeWithText("SIN-26-000083").performScrollTo().performClick()
        rule.onNodeWithText("Resultado: Rechazado").assertExists()
        rule.onNodeWithText("Resolución definitiva").assertExists()
        rule.onNodeWithText("Pedro C. · Interna").assertExists()
        rule.onNodeWithText("Pedro C. · Visible al asegurado").assertExists()
        rule.onNodeWithText("Entregado localmente").assertExists()
        rule.onNodeWithText("Pendiente").assertExists()
    }

    @Test fun selectorDeEquipoEnviaElPerfilElegido() {
        val elegido = AtomicReference(Liquidador.HOGAR)
        mostrar(casos = emptyList(), onSeleccionar = { elegido.set(it) })
        rule.onNodeWithText("Vehículos").performClick()
        assertEquals(Liquidador.VEHICULOS, elegido.get())
    }
}
