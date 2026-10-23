package cl.duocuc.segurosbpo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.components.CampoContrasenaPreview
import cl.duocuc.segurosbpo.ui.components.GrupoRadio
import cl.duocuc.segurosbpo.ui.components.ResumenGraficosCasos
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class RecorridosCondicionalesComposeTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(id: Long, etapa: EtapaSiniestro) = Siniestro(
        id = id,
        numero = "SIN-26-00" + id,
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Prueba de gráficos",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026 12:00"
    )

    @Test fun temaSeRecomponeAlCambiarModoOscuroYColoresDinamicos() {
        val oscuro = mutableStateOf(false)
        val dinamico = mutableStateOf(false)
        rule.setContent {
            SegurosBpoTheme(darkTheme = oscuro.value, dynamicColor = dinamico.value) {
                Text("Tema actualizado")
            }
        }
        rule.onNodeWithText("Tema actualizado").assertExists()
        rule.runOnIdle { oscuro.value = true }
        rule.onNodeWithText("Tema actualizado").assertExists()
        rule.runOnIdle { dinamico.value = true }
        rule.onNodeWithText("Tema actualizado").assertExists()
        rule.runOnIdle { oscuro.value = false }
        rule.onNodeWithText("Tema actualizado").assertExists()
        rule.runOnIdle { dinamico.value = false }
        rule.onNodeWithText("Tema actualizado").assertExists()
    }

    @Test fun formularioConViewModelExplicitoRespetaElAseguradoActual() {
        val vm = FormularioViewModel()
        rule.setContent {
            FormularioScreen(
                aseguradoActual = Asegurado.JORGE,
                onRegistroExitoso = { "SIN-26-009999" },
                onVolver = {},
                onVerMisSiniestros = {},
                viewModel = vm
            )
        }
        rule.onNodeWithText("Volver a mis siniestros").assertExists()
        rule.waitForIdle()
        assertEquals(Asegurado.JORGE, vm.uiState.value.asegurado)
    }

    @Test fun radioPermiteVolverAPulsarLaOpcionYaSeleccionada() {
        val pulsaciones = AtomicInteger()
        rule.setContent {
            var seleccion by remember { mutableStateOf("Uno") }
            Column {
                GrupoRadio(
                    opciones = listOf("Uno", "Dos"),
                    seleccionada = seleccion,
                    onSeleccionar = {
                        seleccion = it
                        pulsaciones.incrementAndGet()
                    },
                    textoDe = { it },
                    modifier = Modifier.padding(4.dp)
                )
                Text("Seleccionado: " + seleccion)
            }
        }
        rule.onNodeWithText("Dos").performClick()
        rule.onNodeWithText("Seleccionado: Dos").assertExists()
        rule.onNodeWithText("Dos").performClick()
        assertEquals(2, pulsaciones.get())
    }

    @Test fun graficosActualizanResultadosCuandoCambianLosCasos() {
        val lista = mutableStateOf(emptyList<Siniestro>())
        rule.setContent {
            ResumenGraficosCasos(casos = lista.value, equipo = "Vehículos", modifier = Modifier.padding(8.dp))
        }
        rule.onNodeWithText("No hay casos asignados para mostrar en los gráficos.").assertExists()
        rule.runOnIdle { lista.value = listOf(caso(601, EtapaSiniestro.RECIBIDO)) }
        rule.onNodeWithText("Total: 1").assertExists()
        rule.onNodeWithText("Abiertos: 1").assertExists()
        rule.runOnIdle {
            lista.value = listOf(
                caso(601, EtapaSiniestro.RECIBIDO),
                caso(602, EtapaSiniestro.CERRADO)
            )
        }
        rule.onNodeWithText("Total: 2").assertExists()
        rule.onNodeWithText("Cerrados: 1").assertExists()
    }

    @Test fun visibilidadDePreviewVuelveAOcultarLaClave() {
        rule.setContent { CampoContrasenaPreview() }
        rule.onNodeWithText("Caracteres: 7").assertExists()
        rule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        rule.onNodeWithContentDescription("Ocultar Contraseña").performClick()
        rule.onNodeWithContentDescription("Mostrar contraseña").assertExists()
    }

    @Test fun graficoPermaneceCorrectoSiCambiaSoloElNombreDelEquipo() {
        val titulo = mutableStateOf("Vehículos")
        val casos = listOf(
            caso(701, EtapaSiniestro.RECIBIDO),
            caso(702, EtapaSiniestro.CERRADO)
        )
        rule.setContent {
            ResumenGraficosCasos(casos = casos, equipo = titulo.value)
        }
        rule.onNodeWithText("Resumen del equipo Vehículos").assertExists()
        rule.onNodeWithText("Abiertos: 1").assertExists()
        rule.runOnIdle { titulo.value = "Equipo de pruebas" }
        rule.onNodeWithText("Resumen del equipo Equipo de pruebas").assertExists()
        rule.onNodeWithText("Cerrados: 1").assertExists()
        rule.runOnIdle { titulo.value = "Vehículos" }
        rule.onNodeWithText("Abiertos: 1").assertExists()
    }

    @Test fun temaConservaContenidoDuranteRecomposicionesSinCambiarPaleta() {
        val contador = mutableStateOf(0)
        rule.setContent {
            SegurosBpoTheme {
                Text("Recomposición: " + contador.value)
            }
        }
        rule.onNodeWithText("Recomposición: 0").assertExists()
        rule.runOnIdle { contador.value = 1 }
        rule.onNodeWithText("Recomposición: 1").assertExists()
        rule.runOnIdle { contador.value = 2 }
        rule.onNodeWithText("Recomposición: 2").assertExists()
    }

}
