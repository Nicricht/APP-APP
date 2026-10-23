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
import cl.duocuc.segurosbpo.ui.components.GraficoSituacion
import cl.duocuc.segurosbpo.model.ResumenCasos
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import cl.duocuc.segurosbpo.model.SolicitudDocumento
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


    @Test fun cambiosDeSolicitudesNoReinicianCargaDelMismoSiniestro() {
        val pendientes = mutableStateOf(emptyList<SolicitudDocumento>())
        val cargas = AtomicInteger(0)
        val actual = caso(803, EtapaSiniestro.EVALUACION)
        rule.setContent {
            DetalleSiniestroScreen(
                siniestro = actual,
                solicitudes = pendientes.value,
                onCargarSolicitudes = { cargas.incrementAndGet() },
                onVolver = {},
                onEditar = {},
                onEvidencias = {},
                onHistorial = {}
            )
        }
        rule.waitForIdle()
        assertEquals(1, cargas.get())
        rule.runOnIdle {
            pendientes.value = listOf(
                SolicitudDocumento(1, 803, "Constancia", "PDF", "20-10-2026", null)
            )
        }
        rule.onNodeWithText("Tienes 1 documento(s) pendiente(s) por entregar.").assertExists()
        rule.waitForIdle()
        assertEquals(1, cargas.get())
        rule.runOnIdle { pendientes.value = emptyList() }
        rule.onNodeWithText("Tienes 1 documento(s) pendiente(s) por entregar.").assertDoesNotExist()
        assertEquals(1, cargas.get())
    }

    @Test fun formularioRecibeCambiosDelAseguradoSinRecrearViewModel() {
        val asegurado = mutableStateOf(Asegurado.CAMILA)
        val vm = FormularioViewModel()
        rule.setContent {
            FormularioScreen(
                aseguradoActual = asegurado.value,
                onRegistroExitoso = { "SIN-26-009999" },
                onVolver = {},
                onVerMisSiniestros = {},
                viewModel = vm
            )
        }
        rule.waitForIdle()
        assertEquals(Asegurado.CAMILA, vm.uiState.value.asegurado)
        rule.runOnIdle { asegurado.value = Asegurado.JORGE }
        rule.waitForIdle()
        assertEquals(Asegurado.JORGE, vm.uiState.value.asegurado)
        rule.runOnIdle { asegurado.value = Asegurado.CAMILA }
        rule.waitForIdle()
        assertEquals(Asegurado.CAMILA, vm.uiState.value.asegurado)
    }

    @Test fun formularioConParametrosSinCambiosPermaneceConsistenteAlActualizarElPadre() {
        val actualizacion = mutableStateOf(0)
        val vm = FormularioViewModel()
        val guardar: (cl.duocuc.segurosbpo.model.FormularioUiState) -> String = { "SIN-26-010000" }
        val sinAccion: () -> Unit = {}
        rule.setContent {
            Column {
                Text("Pantalla: " + actualizacion.value)
                FormularioScreen(
                    aseguradoActual = Asegurado.CAMILA,
                    onRegistroExitoso = guardar,
                    onVolver = sinAccion,
                    onVerMisSiniestros = sinAccion,
                    viewModel = vm
                )
            }
        }
        rule.onNodeWithText("Pantalla: 0").assertExists()
        rule.runOnIdle { actualizacion.value = 1 }
        rule.onNodeWithText("Pantalla: 1").assertExists()
        rule.runOnIdle { actualizacion.value = 2 }
        rule.onNodeWithText("Pantalla: 2").assertExists()
        assertEquals(Asegurado.CAMILA, vm.uiState.value.asegurado)
    }

    @Test fun graficoCircularSeMantieneAlActualizarUnHermanoEnLaComposicion() {
        val contador = mutableStateOf(0)
        val resumen = ResumenCasos(recibidos = 2, evaluacion = 1, liquidacion = 0, cerrados = 1)
        rule.setContent {
            Column {
                Text("Versión de la vista: " + contador.value)
                GraficoSituacion(resumen)
            }
        }
        rule.onNodeWithText("Total: 4").assertExists()
        rule.onNodeWithText("Abiertos: 3").assertExists()
        rule.runOnIdle { contador.value = 1 }
        rule.onNodeWithText("Versión de la vista: 1").assertExists()
        rule.onNodeWithText("Cerrados: 1").assertExists()
        rule.runOnIdle { contador.value = 2 }
        rule.onNodeWithText("Versión de la vista: 2").assertExists()
        rule.onNodeWithText("Total: 4").assertExists()
    }
}
