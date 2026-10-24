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
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class SeguimientoSemanalPantallaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso() = Siniestro(
        id = 73,
        numero = "SIN-26-000073",
        asegurado = Asegurado.JORGE,
        compania = Compania.LITORAL,
        tipo = TipoSiniestro.DANO_AGUA,
        fechaOcurrencia = "20-09-2026",
        fechaReporte = "21-09-2026",
        comuna = "Santiago",
        descripcion = "Daño por agua en vivienda",
        huboTerceros = false,
        etapa = EtapaSiniestro.EVALUACION,
        ultimaActualizacion = "21-09-2026 10:00",
        liquidadorAsignado = Liquidador.HOGAR
    )

    private fun mostrar(
        seguimientosVencidos: Set<Long>,
        guardar: (Long, String, Boolean) -> String? = { _, _, _ -> null }
    ) {
        rule.setContent {
            CasosLiquidadorScreen(
                liquidador = Liquidador.HOGAR,
                casos = listOf(caso()),
                gestiones = emptyList(),
                solicitudes = emptyList(),
                seguimientosVencidos = seguimientosVencidos,
                onSolicitarDocumento = { _, _, _ -> null },
                onSeleccionarLiquidador = {},
                onCargarGestiones = {},
                onRegistrarGestion = guardar,
                onAvanzar = { _, _, _ -> null },
                onCerrarAnticipado = { _, _, _ -> null },
                onVolver = {}
            )
        }
    }

    @Test fun avisaSeguimientoVencidoEnListadoYDetalle() {
        mostrar(setOf(73))
        val mensaje = "Seguimiento pendiente: registra una gestión visible"
        rule.onNodeWithText(mensaje).assertExists()
        rule.onNodeWithText("SIN-26-000073").performScrollTo().performClick()
        rule.onNodeWithText("Gestionar: SIN-26-000073").assertExists()
        rule.onNodeWithText(mensaje).assertExists()
        rule.onNodeWithText("Volver a la lista de casos").performScrollTo().performClick()
        rule.onNodeWithText(mensaje).assertExists()
    }

    @Test fun casosConSeguimientoAlDiaNoMuestranAlerta() {
        mostrar(emptySet())
        val mensaje = "Seguimiento pendiente: registra una gestión visible"
        rule.onNodeWithText(mensaje).assertDoesNotExist()
        rule.onNodeWithText("SIN-26-000073").performScrollTo().performClick()
        rule.onNodeWithText(mensaje).assertDoesNotExist()
    }

    @Test fun sinNovedadesRequiereConfirmacionYQuedaVisible() {
        val guardado = AtomicReference<String?>()
        mostrar(setOf(73)) { id, descripcion, visible ->
            guardado.set("$id:$descripcion:$visible")
            null
        }
        rule.onNodeWithText("SIN-26-000073").performScrollTo().performClick()
        rule.onNodeWithText("Completar con Sin novedades").performScrollTo().performClick()
        assertNull(guardado.get())
        rule.onNodeWithText("Registrar gestión").performScrollTo().performClick()
        assertEquals("73:Sin novedades:true", guardado.get())
    }
}
