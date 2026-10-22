package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class SelectorAseguradoCoberturaTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun listaDeAseguradosPermiteSeleccionarYCerrarDialogo() {
        val seleccionado = AtomicReference(Asegurado.CAMILA)
        composeRule.setContent {
            MisSiniestrosScreen(
                asegurado = Asegurado.CAMILA,
                siniestros = emptyList(),
                busqueda = "",
                avisosNoLeidos = 3,
                onVerAvisos = {},
                onBuscar = {},
                onCambiarAsegurado = { seleccionado.set(it) },
                onReportar = {},
                onGestionar = {},
                onVerDetalle = {}
            )
        }
        composeRule.onNodeWithText("Avisos (3)").assertExists()
        composeRule.onNodeWithText("Cambiar asegurado").performClick()
        composeRule.onNodeWithText("Selecciona un asegurado ficticio").assertExists()
        composeRule.onNodeWithText(Asegurado.JORGE.etiqueta).performClick()
        assertEquals(Asegurado.JORGE, seleccionado.get())
        composeRule.onNodeWithText("Selecciona un asegurado ficticio").assertDoesNotExist()
        composeRule.onNodeWithText("Cambiar asegurado").performClick()
        composeRule.onNodeWithText("Cerrar").performClick()
        composeRule.onNodeWithText("Selecciona un asegurado ficticio").assertDoesNotExist()
    }

    @Test fun tarjetaMuestraCasoTardioYPermiteAbrirDetalle() {
        val elegido = AtomicReference(-1L)
        val caso = Siniestro(
            id = 987L,
            numero = "SIN-26-009987",
            asegurado = Asegurado.CAMILA,
            compania = Compania.CORDILLERA,
            tipo = TipoSiniestro.CHOQUE,
            fechaOcurrencia = "01-09-2026",
            fechaReporte = "20-10-2026",
            comuna = "Maipú",
            descripcion = "Colisión ficticia",
            huboTerceros = false,
            etapa = EtapaSiniestro.RECIBIDO,
            ultimaActualizacion = "20-10-2026 10:00"
        )
        composeRule.setContent {
            MisSiniestrosScreen(
                asegurado = Asegurado.CAMILA,
                siniestros = listOf(caso),
                busqueda = "",
                avisosNoLeidos = 0,
                onVerAvisos = {},
                onBuscar = {},
                onCambiarAsegurado = {},
                onReportar = {},
                onGestionar = {},
                onVerDetalle = { elegido.set(it) }
            )
        }
        composeRule.onNodeWithText("Avisos").assertExists()
        composeRule.onNodeWithText("Reporte fuera de plazo").assertExists()
        composeRule.onNodeWithText(caso.numero).performClick()
        assertEquals(caso.id, elegido.get())
        assertTrue(elegido.get() > 0)
    }
}
