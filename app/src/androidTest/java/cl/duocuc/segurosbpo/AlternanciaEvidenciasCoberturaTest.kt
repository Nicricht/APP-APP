package cl.duocuc.segurosbpo

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class AlternanciaEvidenciasCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private val caso = Siniestro(
        id = 840, numero = "SIN-26-000840", asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA, tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026", fechaReporte = "21-10-2026",
        comuna = "Santiago", descripcion = "Caso de evidencias con varias fotos",
        huboTerceros = false, etapa = EtapaSiniestro.EVALUACION,
        ultimaActualizacion = "22-10-2026 22:00"
    )

    @Test fun cambiaVistaPreviaEntreDosImagenesSinMostrarLaAnterior() {
        val fotoFrontal = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
        val fotoTrasera = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val lecturas = AtomicInteger(0)
        try {
            rule.setContent {
                EvidenciasScreen(
                    siniestro = caso, asegurado = Asegurado.CAMILA,
                    evidencias = listOf(
                        Evidencia(10, 840, "Vista frontal", "frente.jpg", "image/jpeg", 320, "22-10-2026"),
                        Evidencia(11, 840, "Vista trasera", "detras.png", "image/png", 450, "22-10-2026")
                    ),
                    onVolver = {}, onCargar = {},
                    onCargarImagen = { id ->
                        lecturas.incrementAndGet()
                        if (id == 10L) fotoFrontal else fotoTrasera
                    },
                    onGuardarFoto = { _, _ -> null },
                    onGuardarArchivo = { _, _ -> null }
                )
            }
            rule.onAllNodesWithText("Ver imagen")[0].performScrollTo().performClick()
            rule.waitUntil(12000) {
                rule.onAllNodesWithContentDescription("Vista previa: Vista frontal")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            rule.onAllNodesWithText("Ver imagen")[1].performScrollTo().performClick()
            rule.waitUntil(12000) {
                rule.onAllNodesWithContentDescription("Vista previa: Vista trasera")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            assertEquals(
                0,
                rule.onAllNodesWithContentDescription("Vista previa: Vista frontal")
                    .fetchSemanticsNodes().size
            )
            assertEquals(2, lecturas.get())
        } finally {
            fotoFrontal.recycle()
            fotoTrasera.recycle()
        }
    }

    @Test fun solicitudesSinIndicacionesNoAgreganTextoYPermitenPrepararEntrega() {
        val regresos = AtomicInteger(0)
        val cargas = AtomicInteger(0)
        rule.setContent {
            EvidenciasScreen(
                siniestro = caso,
                asegurado = Asegurado.CAMILA,
                evidencias = emptyList(),
                solicitudes = listOf(
                    SolicitudDocumento(12, 840, "Informe", "", "22-10-2026", null),
                    SolicitudDocumento(13, 840, "Boleta", "", "22-10-2026", "22-10-2026")
                ),
                onVolver = { regresos.incrementAndGet() },
                onCargar = { cargas.incrementAndGet() },
                onCargarImagen = { null },
                onGuardarFoto = { _, _ -> null },
                onGuardarArchivo = { _, _ -> null }
            )
        }
        rule.onNodeWithText("Pendiente de entrega").assertExists()
        rule.onNodeWithText("Entregado localmente").assertExists()
        rule.onNodeWithText("Preparar entrega de este documento")
            .performScrollTo().performClick()
        rule.onNodeWithText("Tipo de documento *").assertExists()
        rule.onNodeWithText("Volver al siniestro").performScrollTo().performClick()
        rule.waitForIdle()
        assertEquals(1, regresos.get())
        assertEquals(1, cargas.get())
    }
}
