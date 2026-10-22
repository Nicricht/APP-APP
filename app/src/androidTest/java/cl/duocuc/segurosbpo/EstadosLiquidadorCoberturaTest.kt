package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.*
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EstadosLiquidadorCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun presentar(casos: List<Siniestro>) {
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = casos,
                gestiones = emptyList(),
                solicitudes = emptyList(),
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = { _, _, _ -> null },
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = { _, _, _ -> null },
                onVolver = {}
            )
        }
    }

    @Test fun sinCasosSePresentaVacio() {
        presentar(emptyList())
        rule.onNodeWithText("No hay casos asignados.").assertExists()
        rule.onNodeWithText("No hay casos asignados para mostrar en los gráficos.").assertExists()
    }

    @Test fun cerradoNoPermiteModificar() {
        presentar(listOf(Siniestro(
            42, "SIN-26-000042", Asegurado.JORGE, Compania.LITORAL,
            TipoSiniestro.DANO_AGUA, "20-10-2026", "21-10-2026",
            "Maipú", "Caso cerrado", false, EtapaSiniestro.CERRADO,
            "21-10-2026 12:00", Liquidador.HOGAR
        )))
        rule.onNodeWithText("SIN-26-000042").performScrollTo().performClick()
        rule.onNodeWithText("Este siniestro está cerrado. No se admiten modificaciones.").assertExists()
        rule.onNodeWithText("Resultado: No disponible").assertExists()
        rule.onNodeWithText("Volver a la lista de casos").performScrollTo().performClick()
        rule.onNodeWithText("SIN-26-000042").assertExists()
    }
}
