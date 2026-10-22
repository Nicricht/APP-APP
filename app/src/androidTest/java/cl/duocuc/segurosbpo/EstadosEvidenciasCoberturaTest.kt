package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
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
class EstadosEvidenciasCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro): Siniestro = Siniestro(
        id = 61,
        numero = "SIN-26-000061",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Siniestro de prueba",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026 13:00"
    )

    private fun mostrar(
        siniestro: Siniestro?,
        solicitudes: List<SolicitudDocumento> = emptyList(),
        evidencias: List<Evidencia> = emptyList()
    ) {
        rule.setContent {
            EvidenciasScreen(
                siniestro = siniestro,
                asegurado = Asegurado.CAMILA,
                evidencias = evidencias,
                solicitudes = solicitudes,
                onVolver = {},
                onCargar = {},
                onCargarImagen = { null },
                onGuardarFoto = { _, _ -> null },
                onGuardarArchivo = { _, _ -> null }
            )
        }
    }

    @Test fun pantallaSinSiniestroNoPermiteAdjuntar() {
        mostrar(null)
        rule.onNodeWithText("Este siniestro no pertenece al asegurado seleccionado").assertExists()
        rule.onNodeWithText("Tomar fotografía").assertDoesNotExist()
    }

    @Test fun cerradoMuestraSolicitudYProhibeAdjuntar() {
        mostrar(
            caso(EtapaSiniestro.CERRADO),
            listOf(SolicitudDocumento(1, 61, "Constancia", "PDF", "22-10-2026", null))
        )
        rule.onNodeWithText("Pendiente de entrega").assertExists()
        rule.onNodeWithText("No se pueden agregar evidencias a un siniestro cerrado.").assertExists()
        rule.onNodeWithText("Preparar entrega de este documento").assertDoesNotExist()
        rule.onNodeWithText("Adjuntar PDF").assertDoesNotExist()
    }

    @Test fun aseguradoPuedePrepararDocumentoSolicitado() {
        mostrar(
            caso(EtapaSiniestro.EVALUACION),
            listOf(SolicitudDocumento(1, 61, "Constancia", "Enviar archivo", "22-10-2026", null))
        )
        rule.onNodeWithText("Preparar entrega de este documento").performScrollTo().performClick()
        rule.onNodeWithText("Tipo de documento *").assertExists()
        rule.onNodeWithText("Adjuntar PDF").assertExists()
    }

    @Test fun limiteQuinceArchivosBloqueaNuevosAdjuntos() {
        val lista = (1..15).map { indice ->
            Evidencia(indice.toLong(), 61, "Documento " + indice, "archivo.pdf", "application/pdf", 100, "22-10-2026")
        }
        mostrar(caso(EtapaSiniestro.EVALUACION), evidencias = lista)
        rule.onNodeWithText("Evidencias guardadas: 15/15").assertExists()
        rule.onNodeWithText("Tomar fotografía").assertIsNotEnabled()
        rule.onNodeWithText("Elegir de la galería").assertIsNotEnabled()
        rule.onNodeWithText("Adjuntar PDF").assertIsNotEnabled()
    }
}
