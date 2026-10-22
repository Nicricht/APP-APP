package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumenGestionCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso() = Siniestro(
        id = 81,
        numero = "SIN-26-000081",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Maipú",
        descripcion = "Daño por agua",
        huboTerceros = false,
        etapa = EtapaSiniestro.EVALUACION,
        ultimaActualizacion = "21-10-2026 12:00",
        liquidadorAsignado = Liquidador.HOGAR
    )

    private fun mostrar(solicitudes: List<SolicitudDocumento>, gestiones: List<Gestion>) {
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso()),
                gestiones = gestiones,
                solicitudes = solicitudes,
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = { _, _, _ -> null },
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = { _, _, _ -> null },
                onVolver = {}
            )
        }
        rule.onNodeWithText("SIN-26-000081").performScrollTo().performClick()
    }

    @Test fun distingueDocumentosPendientesYEntregados() {
        mostrar(
            solicitudes = listOf(
                SolicitudDocumento(1, 81, "Boleta", "Adjuntar PDF", "22-10-2026", null),
                SolicitudDocumento(2, 81, "Fotografía", "", "22-10-2026", "22-10-2026")
            ),
            gestiones = emptyList()
        )
        rule.onNodeWithText("Boleta").assertExists()
        rule.onNodeWithText("Fotografía").assertExists()
        rule.onNodeWithText("Pendiente").assertExists()
        rule.onNodeWithText("Entregado localmente").assertExists()
        rule.onNodeWithText("Faltan 1 documentos. Deben entregarse antes de avanzar.").assertExists()
    }

    @Test fun distingueGestionesInternasYVisibles() {
        mostrar(
            solicitudes = emptyList(),
            gestiones = listOf(
                Gestion(1, 81, "22-10-2026", "Liquidador QA", "Análisis privado", false),
                Gestion(2, 81, "22-10-2026", "Liquidador QA", "Estado informado", true)
            )
        )
        rule.onNodeWithText("Análisis privado").assertExists()
        rule.onNodeWithText("Estado informado").assertExists()
        rule.onNodeWithText("Liquidador QA · Interna").assertExists()
        rule.onNodeWithText("Liquidador QA · Visible al asegurado").assertExists()
        rule.onNodeWithText("No hay documentos solicitados.").assertExists()
    }
}
