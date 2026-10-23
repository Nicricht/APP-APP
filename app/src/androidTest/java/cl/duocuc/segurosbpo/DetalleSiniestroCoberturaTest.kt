package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
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
class DetalleSiniestroCoberturaTest {
    @get:Rule val rule = createComposeRule()
    private val cargados = AtomicInteger(0)
    private val consultas = AtomicInteger(0)

    private fun caso(etapa: EtapaSiniestro, fecha: String = "20-10-2026"): Siniestro =
        Siniestro(
            id = 91,
            numero = "SIN-26-000091",
            asegurado = Asegurado.CAMILA,
            compania = Compania.CORDILLERA,
            tipo = TipoSiniestro.CHOQUE,
            fechaOcurrencia = fecha,
            fechaReporte = "22-10-2026",
            comuna = "Santiago",
            descripcion = "Colisión de prueba",
            huboTerceros = true,
            etapa = etapa,
            ultimaActualizacion = "22-10-2026 12:00"
        )

    private fun mostrar(
        siniestro: Siniestro?,
        solicitudes: List<SolicitudDocumento> = emptyList()
    ) {
        rule.setContent {
            DetalleSiniestroScreen(
                siniestro = siniestro,
                solicitudes = solicitudes,
                onCargarSolicitudes = { cargados.incrementAndGet() },
                onVolver = {},
                onEditar = { consultas.incrementAndGet() },
                onEvidencias = { consultas.incrementAndGet() },
                onHistorial = { consultas.incrementAndGet() }
            )
        }
    }

    @Test fun noExisteElCasoYNoSeCarganDocumentos() {
        mostrar(null)
        rule.onNodeWithText("No se encontró el siniestro de este asegurado").assertExists()
        assertEquals(0, cargados.get())
    }

    @Test fun recibidoPermiteEditarYConsultarHistorialYEvidencias() {
        mostrar(caso(EtapaSiniestro.RECIBIDO))
        rule.waitForIdle()
        assertEquals(1, cargados.get())
        rule.onNodeWithText("Corregir descripción").performScrollTo().performClick()
        rule.onNodeWithText("Ver historial").performScrollTo().performClick()
        rule.onNodeWithText("Documentos y evidencias").performScrollTo().performClick()
        assertEquals(3, consultas.get())
        rule.onNodeWithText("Estamos revisando tu reporte para asignarlo a un liquidador. No necesitas hacer nada por ahora.").assertExists()
    }

    @Test fun enEvaluacionPresentaDocumentosPendientes() {
        mostrar(
            caso(EtapaSiniestro.EVALUACION),
            listOf(SolicitudDocumento(1, 91, "Constancia", "PDF", "22-10-2026", null))
        )
        rule.onNodeWithText("Tienes 1 documento(s) pendiente(s) por entregar.").assertExists()
        rule.onNodeWithText("Pendiente: Constancia").assertExists()
        rule.onNodeWithText("Adjunta los documentos solicitados para que tu caso pueda continuar.").assertExists()
    }

    @Test fun enLiquidacionSinPendientesMuestraSiguientePaso() {
        mostrar(caso(EtapaSiniestro.LIQUIDACION))
        rule.onNodeWithText("Estamos definiendo la resolución con tu compañía de seguros.").assertExists()
    }

    @Test fun cerradoMuestraResultadoYFechaAntigua() {
        val cerrado = caso(EtapaSiniestro.CERRADO, "02-12-2001").copy(
            resultadoCierre = "Rechazado",
            mensajeCierre = "Decisión registrada"
        )
        mostrar(cerrado)
        rule.onNodeWithText("Resultado: Rechazado").assertExists()
        rule.onNodeWithText("Mensaje de cierre: Decisión registrada").assertExists()
        rule.onNodeWithText("Reporte fuera de plazo (más de 30 días desde el siniestro)").assertExists()
        rule.onNodeWithText("Tu caso terminó. Revisa el resultado registrado.").assertExists()
    }

    @Test fun casoAsignadoMuestraElNombreDelLiquidadorResponsable() {
        val asignado = caso(EtapaSiniestro.EVALUACION).copy(
            liquidadorAsignado = Liquidador.VEHICULOS
        )
        mostrar(asignado)
        rule.onNodeWithText("Liquidador: " + Liquidador.VEHICULOS.nombre).assertExists()
        rule.waitForIdle()
        assertEquals(1, cargados.get())
    }
}
