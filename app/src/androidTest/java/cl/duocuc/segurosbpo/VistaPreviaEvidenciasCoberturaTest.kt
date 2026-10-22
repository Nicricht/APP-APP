package cl.duocuc.segurosbpo

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VistaPreviaEvidenciasCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private val siniestro = Siniestro(
        id = 74,
        numero = "SIN-26-000074",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Quilicura",
        descripcion = "Daño de prueba",
        huboTerceros = false,
        etapa = EtapaSiniestro.EVALUACION,
        ultimaActualizacion = "22-10-2026 11:00"
    )

    @Test fun imagenGuardadaAbreVistaPrevia() {
        val bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888)
        try {
            rule.setContent {
                EvidenciasScreen(
                    siniestro = siniestro,
                    asegurado = Asegurado.CAMILA,
                    evidencias = listOf(
                        Evidencia(6, 74, "Foto del daño", "foto.jpg", "image/jpeg", 1024, "22-10-2026")
                    ),
                    onVolver = {},
                    onCargar = {},
                    onCargarImagen = { bitmap },
                    onGuardarFoto = { _, _ -> null },
                    onGuardarArchivo = { _, _ -> null }
                )
            }
            rule.onNodeWithText("Ver imagen").performScrollTo().performClick()
            rule.waitUntil(10000) {
                rule.onAllNodesWithContentDescription("Vista previa: Foto del daño")
                    .fetchSemanticsNodes().isNotEmpty()
            }
        } finally {
            bitmap.recycle()
        }
    }

    @Test fun imagenNoDisponibleInformaElProblema() {
        rule.setContent {
            EvidenciasScreen(
                siniestro = siniestro,
                asegurado = Asegurado.CAMILA,
                evidencias = listOf(
                    Evidencia(7, 74, "Fotografía", "foto.png", "image/png", 512, "22-10-2026")
                ),
                onVolver = {},
                onCargar = {},
                onCargarImagen = { null },
                onGuardarFoto = { _, _ -> null },
                onGuardarArchivo = { _, _ -> null }
            )
        }
        rule.onNodeWithText("Ver imagen").performScrollTo().performClick()
        rule.waitUntil(10000) {
            rule.onNodeWithText("No se pudo abrir la imagen")
                .fetchSemanticsNodeOrNull() != null
        }
    }

    @Test fun documentoEntregadoNoSolicitaOtraEntregaYSeLista() {
        rule.setContent {
            EvidenciasScreen(
                siniestro = siniestro,
                asegurado = Asegurado.CAMILA,
                evidencias = listOf(
                    Evidencia(8, 74, "Boleta", "boleta.pdf", "application/pdf", 1024, "22-10-2026")
                ),
                solicitudes = listOf(
                    SolicitudDocumento(4, 74, "Boleta", "Documento recibido", "21-10-2026", "22-10-2026")
                ),
                onVolver = {},
                onCargar = {},
                onCargarImagen = { null },
                onGuardarFoto = { _, _ -> null },
                onGuardarArchivo = { _, _ -> null }
            )
        }
        rule.onNodeWithText("Entregado localmente").assertExists()
        rule.onNodeWithText("Documento recibido").assertExists()
        rule.onNodeWithText("boleta.pdf").assertExists()
        rule.onNodeWithText("Preparar entrega de este documento").assertDoesNotExist()
        rule.onNodeWithText("Ver imagen").assertDoesNotExist()
    }
}
