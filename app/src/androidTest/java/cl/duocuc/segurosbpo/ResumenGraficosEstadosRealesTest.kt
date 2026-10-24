package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.components.GraficoEvolucionCasos
import cl.duocuc.segurosbpo.ui.components.GraficosDistribucionCasos
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumenGraficosEstadosRealesTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(
        id: Long,
        tipo: TipoSiniestro,
        etapa: EtapaSiniestro,
        fecha: String
    ) = Siniestro(
        id = id,
        numero = "SIN-26-0000$id",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = tipo,
        fechaOcurrencia = fecha,
        fechaReporte = fecha,
        comuna = "Maipú",
        descripcion = "Caso ficticio $id para estadística",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = fecha
    )

    @Test fun resumenVacioPredeterminadoNoMuestraGraficasInventadas() {
        rule.setContent {
            ResumenGraficosCasos(casos = emptyList(), equipo = "Vehículos")
        }
        rule.onNodeWithText("Resumen del equipo Vehículos").assertExists()
        rule.onNodeWithText("Casos asignados a este liquidador: 0").assertExists()
        rule.onNodeWithText("No hay casos asignados para mostrar en los gráficos.").assertExists()
        rule.onNodeWithText("Casos por etapa").assertDoesNotExist()
        rule.onNodeWithText("Abiertos y cerrados").assertDoesNotExist()
    }

    @Test fun totalesCoincidenConEstadosYTiposSinMezclarCasos() {
        val casos = listOf(
            caso(81, TipoSiniestro.CHOQUE, EtapaSiniestro.RECIBIDO, "21-10-2026"),
            caso(82, TipoSiniestro.CHOQUE, EtapaSiniestro.EVALUACION, "22-10-2026"),
            caso(83, TipoSiniestro.DANO_AGUA, EtapaSiniestro.CERRADO, "20-09-2026")
        )
        rule.setContent { ResumenGraficosCasos(casos = casos, equipo = "Mixto") }
        rule.onNodeWithText("Casos asignados a este liquidador: 3").assertExists()
        rule.onNodeWithText("Abiertos: 2").assertExists()
        rule.onNodeWithText("Cerrados: 1").assertExists()
        rule.onNodeWithText("Total: 3").assertExists()
        rule.onNodeWithText("Vehículo · choque o colisión: 2").assertExists()
        rule.onNodeWithText("Hogar · daño por agua: 1").assertExists()
        rule.onNodeWithText("09/2026: 1").assertExists()
        rule.onNodeWithText("10/2026: 2").assertExists()
    }

    @Test fun casosSoloCerradosPresentanCeroAbiertos() {
        val casos = listOf(
            caso(91, TipoSiniestro.ROBO_HOGAR, EtapaSiniestro.CERRADO, "10-08-2026"),
            caso(92, TipoSiniestro.INCENDIO, EtapaSiniestro.CERRADO, "10-09-2026")
        )
        rule.setContent { ResumenGraficosCasos(casos = casos, equipo = "Hogar") }
        rule.onNodeWithText("Casos asignados a este liquidador: 2").assertExists()
        rule.onNodeWithText("Abiertos: 0").assertExists()
        rule.onNodeWithText("Cerrados: 2").assertExists()
        rule.onNodeWithText("Hogar · robo: 1").assertExists()
        rule.onNodeWithText("Hogar · incendio: 1").assertExists()
    }

    @Test fun distribucionYEvolucionVaciasNoInventanCategoriasNiMeses() {
        rule.setContent {
            GraficosDistribucionCasos(casos = emptyList())
            GraficoEvolucionCasos(casos = emptyList())
        }
        rule.onNodeWithText("Vehículo · choque o colisión: 1").assertDoesNotExist()
        rule.onNodeWithText("10/2026: 1").assertDoesNotExist()
    }
}
