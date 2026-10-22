package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.HistorialSiniestroScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class HistorialPantallaCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso() = Siniestro(
        id = 77,
        numero = "SIN-26-000077",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Choque registrado",
        huboTerceros = false,
        etapa = EtapaSiniestro.EVALUACION,
        ultimaActualizacion = "22-10-2026"
    )

    @Test fun sinCasoNoCargaHistorialYPermiteVolver() {
        val cargas = AtomicInteger(0)
        val vueltas = AtomicInteger(0)
        rule.setContent {
            HistorialSiniestroScreen(
                siniestro = null,
                gestiones = emptyList(),
                onCargar = { cargas.incrementAndGet() },
                onVolver = { vueltas.incrementAndGet() }
            )
        }
        rule.onNodeWithText("No se encontró un siniestro de este asegurado").assertExists()
        rule.onNodeWithText("Volver al detalle").performClick()
        rule.waitForIdle()
        assertEquals(0, cargas.get())
        assertEquals(1, vueltas.get())
    }

    @Test fun casoSinGestionesMuestraExplicacion() {
        val cargas = AtomicInteger(0)
        rule.setContent {
            HistorialSiniestroScreen(
                siniestro = caso(),
                gestiones = emptyList(),
                onCargar = { cargas.incrementAndGet() },
                onVolver = {}
            )
        }
        rule.onNodeWithText("Todavía no existen movimientos registrados.").assertExists()
        rule.waitForIdle()
        assertEquals(1, cargas.get())
    }

    @Test fun gestionesVisiblesMuestranResponsableYDetalle() {
        val gestion = Gestion(
            id = 9,
            siniestroId = 77,
            fecha = "22-10-2026 12:00",
            responsable = "Liquidador QA",
            descripcion = "Se verificó la documentación",
            visibleAsegurado = true
        )
        rule.setContent {
            HistorialSiniestroScreen(
                siniestro = caso(),
                gestiones = listOf(gestion),
                onCargar = {},
                onVolver = {}
            )
        }
        rule.onNodeWithText("Se verificó la documentación").assertExists()
        rule.onNodeWithText("Responsable: Liquidador QA").assertExists()
        rule.onNodeWithText("SIN-26-000077").assertExists()
    }
}
