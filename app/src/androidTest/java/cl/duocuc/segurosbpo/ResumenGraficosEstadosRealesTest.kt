package cl.duocuc.segurosbpo

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
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

    @Test fun unaRecomposicionAjenaALosCasosNoAlteraLosTotalesDelResumen() {
        val ronda = mutableStateOf(0)
        val cartera = listOf(
            caso(131, TipoSiniestro.CHOQUE, EtapaSiniestro.EVALUACION, "20-10-2026"),
            caso(132, TipoSiniestro.DANO_AGUA, EtapaSiniestro.CERRADO, "20-10-2026")
        )
        rule.setContent {
            Column {
                Text("Ronda visual: ${ronda.value}")
                ResumenGraficosCasos(casos = cartera, equipo = "Mixto")
            }
        }
        rule.onNodeWithText("Casos asignados a este liquidador: 2").assertExists()
        rule.onNodeWithText("Abiertos: 1").assertExists()
        rule.runOnIdle { ronda.value = 1 }
        rule.onNodeWithText("Ronda visual: 1").assertExists()
        rule.onNodeWithText("Casos asignados a este liquidador: 2").assertExists()
        rule.onNodeWithText("Cerrados: 1").assertExists()
    }

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

    @Test fun indicadoresReaccionanAlCambioDeCarteraSinPerderContexto() {
        val reportes = mutableStateOf(emptyList<Siniestro>())
        rule.setContent {
            ResumenGraficosCasos(
                casos = reportes.value,
                equipo = "Hogar",
                modifier = Modifier,
                titulo = "Revisión de cartera",
                descripcion = "Siniestros del período"
            )
        }
        rule.onNodeWithText("Revisión de cartera").assertExists()
        rule.onNodeWithText("Siniestros del período: 0").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
        rule.runOnIdle {
            reportes.value = listOf(
                caso(101, TipoSiniestro.DANO_AGUA, EtapaSiniestro.EVALUACION, "20-10-2026"),
                caso(102, TipoSiniestro.INCENDIO, EtapaSiniestro.CERRADO, "20-09-2026")
            )
        }
        rule.onNodeWithText("Revisión de cartera").assertExists()
        rule.onNodeWithText("Siniestros del período: 2").assertExists()
        rule.onNodeWithText("Abiertos: 1").assertExists()
        rule.onNodeWithText("Cerrados: 1").assertExists()
        rule.onNodeWithText("Hogar · daño por agua: 1").assertExists()
        rule.onNodeWithText("Hogar · incendio: 1").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertDoesNotExist()
        rule.runOnIdle { reportes.value = emptyList() }
        rule.onNodeWithText("Siniestros del período: 0").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
        rule.onNodeWithText("Abiertos: 1").assertDoesNotExist()
    }

    @Test fun laEtapaLiquidacionSeIncluyeEnLosTotalesSinConfundirseConCierre() {
        val casos = listOf(
            caso(111, TipoSiniestro.CRISTALES, EtapaSiniestro.LIQUIDACION, "21-10-2026"),
            caso(112, TipoSiniestro.ROBO_VEHICULO, EtapaSiniestro.RECIBIDO, "21-10-2026")
        )
        rule.setContent {
            ResumenGraficosCasos(
                casos = casos,
                equipo = "Vehículos",
                modifier = Modifier,
                titulo = "Estado de liquidación",
                descripcion = "Casos revisados"
            )
        }
        rule.onNodeWithText("Estado de liquidación").assertExists()
        rule.onNodeWithText("Casos revisados: 2").assertExists()
        rule.onNodeWithText("Abiertos: 2").assertExists()
        rule.onNodeWithText("Cerrados: 0").assertExists()
        rule.onNodeWithText("Vehículo · rotura de cristales: 1").assertExists()
        rule.onNodeWithText("Vehículo · robo: 1").assertExists()
    }

    @Test fun tituloPersonalizadoConDescripcionPredeterminadaConservaTotales() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = listOf(
                    caso(121, TipoSiniestro.DANO_AGUA, EtapaSiniestro.EVALUACION, "20-10-2026")
                ),
                equipo = "Hogar",
                titulo = "Panel de seguimiento"
            )
        }
        rule.onNodeWithText("Panel de seguimiento").assertExists()
        rule.onNodeWithText("Casos asignados a este liquidador: 1").assertExists()
        rule.onNodeWithText("Abiertos: 1").assertExists()
        rule.onNodeWithText("Cerrados: 0").assertExists()
    }

    @Test fun descripcionPersonalizadaSinTituloNoCambiaLaCabeceraDelEquipo() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = emptyList(),
                equipo = "Vehículos",
                descripcion = "Casos revisados en auditoría"
            )
        }
        rule.onNodeWithText("Resumen del equipo Vehículos").assertExists()
        rule.onNodeWithText("Casos revisados en auditoría: 0").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
        rule.onNodeWithText("Casos por etapa").assertDoesNotExist()
    }

    @Test fun modificadorExplicitoConValoresPredeterminadosPresentaElEquipoCorrecto() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = emptyList(),
                equipo = "Hogar",
                modifier = Modifier
            )
        }
        rule.onNodeWithText("Resumen del equipo Hogar").assertExists()
        rule.onNodeWithText("Casos asignados a este liquidador: 0").assertExists()
        rule.onNodeWithText("No hay casos asignados para mostrar en los gráficos.").assertExists()
    }

    @Test fun modificadorYTituloPersonalizadosMantienenDescripcionCorrecta() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = emptyList(),
                equipo = "Hogar",
                modifier = Modifier,
                titulo = "Seguimiento personalizado"
            )
        }
        rule.onNodeWithText("Seguimiento personalizado").assertExists()
        rule.onNodeWithText("Casos asignados a este liquidador: 0").assertExists()
        rule.onNodeWithText("No hay casos asignados para mostrar en los gráficos.").assertExists()
    }

    @Test fun modificadorYDescripcionPersonalizadosMantienenTituloDelEquipo() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = emptyList(),
                equipo = "Vehículos",
                modifier = Modifier,
                descripcion = "Siniestros inspeccionados"
            )
        }
        rule.onNodeWithText("Resumen del equipo Vehículos").assertExists()
        rule.onNodeWithText("Siniestros inspeccionados: 0").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
    }

    @Test fun tituloYDescripcionPersonalizadosSinModificarPresentacionBase() {
        rule.setContent {
            ResumenGraficosCasos(
                casos = emptyList(),
                equipo = "Hogar",
                titulo = "Auditoría de siniestros",
                descripcion = "Casos auditados"
            )
        }
        rule.onNodeWithText("Auditoría de siniestros").assertExists()
        rule.onNodeWithText("Casos auditados: 0").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
    }
}
