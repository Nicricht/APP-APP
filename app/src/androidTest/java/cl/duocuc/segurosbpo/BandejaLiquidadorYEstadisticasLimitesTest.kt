package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EstadisticasCasos
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BandejaLiquidadorYEstadisticasLimitesTest {
    @get:Rule val rule = createComposeRule()

    private fun reporte() = Siniestro(
        id = 84,
        numero = "SIN-26-000084",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Choque ficticio sin liquidador",
        huboTerceros = false,
        etapa = EtapaSiniestro.RECIBIDO,
        ultimaActualizacion = "21-10-2026 12:00",
        liquidadorAsignado = null
    )

    @Test fun tomarCasoSinAsignadorNoSimulaExitoNiAlteraBandeja() {
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.VEHICULOS,
                casos = emptyList(),
                disponibles = listOf(reporte()),
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
        rule.onNodeWithText("Recibido · Sin liquidador asignado").assertExists()
        rule.onNodeWithText("Tomar caso").performScrollTo().performClick()
        rule.waitUntil(7000) {
            rule.onAllNodesWithText("No disponible").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("No disponible").assertExists()
        rule.onNodeWithText("Recibido · Sin liquidador asignado").assertExists()
        rule.onNodeWithText("No hay casos asignados.").assertExists()
    }

    @Test fun resumenSinDatosTieneMensajeAcordeAlContexto() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = emptyList(),
                equipo = "Hogar",
                titulo = "Casos históricos",
                descripcion = "Reportes cerrados"
            )
        }
        rule.onNodeWithText("Casos históricos").assertExists()
        rule.onNodeWithText("Reportes cerrados: 0").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
    }

    @Test fun evolucionMensualIgnoraFechaMalformadaSinAlterarConteoValido() {
        val primero = reporte()
        val segundo = primero.copy(id = 85, numero = "SIN-26-000085", fechaReporte = "21/10/2026")
        val tercero = primero.copy(id = 86, numero = "SIN-26-000086", fechaReporte = "20-09-2026")
        val puntos = EstadisticasCasos.porMes(listOf(primero, segundo, tercero))
        assertEquals(2, puntos.size)
        assertEquals("09/2026", puntos[0].etiqueta)
        assertEquals("10/2026", puntos[1].etiqueta)
        assertEquals(1, puntos[0].cantidad)
        assertEquals(1, puntos[1].cantidad)
    }
}
