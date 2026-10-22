package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class DetalleAlternativoCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun siniestro(etapa: EtapaSiniestro) = Siniestro(
        id = 140,
        numero = "SIN-26-000140",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Incidente de prueba",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026 15:00"
    )

    private fun mostrar(caso: Siniestro, solicitudes: List<SolicitudDocumento> = emptyList()): AtomicInteger {
        val cargas = AtomicInteger(0)
        rule.setContent {
            DetalleSiniestroScreen(
                siniestro = caso,
                solicitudes = solicitudes,
                onCargarSolicitudes = { cargas.incrementAndGet() },
                onVolver = {},
                onEditar = {},
                onEvidencias = {},
                onHistorial = {}
            )
        }
        return cargas
    }

    @Test fun cierreSinInformacionPresentaValoresAlternativos() {
        val cargas = mostrar(siniestro(EtapaSiniestro.CERRADO))
        rule.onNodeWithText("Resultado: No informado").assertExists()
        rule.onNodeWithText("Mensaje de cierre: No disponible").assertExists()
        rule.onNodeWithText("Liquidador: Pendiente de asignación").assertExists()
        rule.onNodeWithText("Corregir descripción").assertDoesNotExist()
        rule.waitForIdle()
        assertEquals(1, cargas.get())
    }

    @Test fun evaluacionSinSolicitudesExplicaElSiguientePaso() {
        mostrar(siniestro(EtapaSiniestro.EVALUACION))
        rule.onNodeWithText("El liquidador está revisando tu caso. Te avisaremos si necesitamos documentos.").assertExists()
        rule.onNodeWithText("Tienes 1 documento(s) pendiente(s) por entregar.").assertDoesNotExist()
    }

    @Test fun documentosEntregadosNoCuentanComoPendientes() {
        mostrar(
            siniestro(EtapaSiniestro.EVALUACION),
            listOf(
                SolicitudDocumento(1, 140, "Boleta", "", "20-10-2026", "21-10-2026"),
                SolicitudDocumento(2, 140, "Constancia", "", "20-10-2026", null)
            )
        )
        rule.onNodeWithText("Tienes 1 documento(s) pendiente(s) por entregar.").assertExists()
        rule.onNodeWithText("Pendiente: Constancia").assertExists()
        rule.onNodeWithText("Pendiente: Boleta").assertDoesNotExist()
    }
}
