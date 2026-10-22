package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LimitesReglasCoberturaTest {
    @Test fun evidenciaRechazaExactamenteLosLimitesQueSuperanLoPermitido() {
        assertNull(ValidadorEvidencia.error("Foto", "image/jpeg", 1, 14))
        assertNull(ValidadorEvidencia.error("Foto", "image/png", ValidadorEvidencia.MAX_BYTES, 14))
        assertNull(ValidadorEvidencia.error("Foto", "image/webp", 20, 0))
        assertNull(ValidadorEvidencia.error("Contrato", "application/pdf", 100, 0))
        assertEquals("El archivo está vacío", ValidadorEvidencia.error("Foto", "image/jpeg", 0, 0))
        assertEquals("El archivo supera los 5 MB permitidos", ValidadorEvidencia.error("Foto", "image/jpeg", ValidadorEvidencia.MAX_BYTES + 1, 0))
        assertEquals("El siniestro ya tiene 15 archivos", ValidadorEvidencia.error("Foto", "image/jpeg", 1, 15))
        assertEquals("Solo se aceptan imágenes JPG, PNG, WEBP o PDF", ValidadorEvidencia.error("Foto", "text/plain", 1, 0))
        assertEquals("Indica qué documento estás adjuntando", ValidadorEvidencia.error(" ", "image/jpeg", 1, 0))
    }

    @Test fun gestionAceptaQuinientosCaracteresYRechazaElExceso() {
        assertEquals("La descripción de la gestión es obligatoria", ReglasEtapa.validarGestion("  "))
        assertNull(ReglasEtapa.validarGestion("X".repeat(500)))
        assertEquals("La descripción no puede superar 500 caracteres", ReglasEtapa.validarGestion("X".repeat(501)))
    }

    @Test fun etapasNoRetrocedenNiReabrenCasosCerrados() {
        assertEquals("No está permitido saltar ni retroceder etapas", ReglasEtapa.validar(
            EtapaSiniestro.CERRADO, EtapaSiniestro.EVALUACION, 0, "", ""
        ))
        assertEquals("No está permitido saltar ni retroceder etapas", ReglasEtapa.validar(
            EtapaSiniestro.LIQUIDACION, EtapaSiniestro.EVALUACION, 0, "", ""
        ))
        assertEquals("Primero debes recibir los documentos pendientes", ReglasEtapa.validar(
            EtapaSiniestro.EVALUACION, EtapaSiniestro.LIQUIDACION, 2, "", ""
        ))
        assertNull(ReglasEtapa.validar(
            EtapaSiniestro.EVALUACION, EtapaSiniestro.LIQUIDACION, 0, "", ""
        ))
    }

    @Test fun cierresDistinguenResultadoInvalidoYJustificacionVacia() {
        assertEquals("Resultado de cierre no permitido para esta etapa", ReglasEtapa.validar(
            EtapaSiniestro.RECIBIDO, EtapaSiniestro.CERRADO, 0, "Aprobado", "No corresponde"
        ))
        assertEquals("El mensaje de cierre es obligatorio", ReglasEtapa.validar(
            EtapaSiniestro.EVALUACION, EtapaSiniestro.CERRADO, 0, "Desistido", " "
        ))
        assertNull(ReglasEtapa.validar(
            EtapaSiniestro.RECIBIDO, EtapaSiniestro.CERRADO, 0, "Desistido", "Desistimiento solicitado"
        ))
        assertNull(ReglasEtapa.validar(
            EtapaSiniestro.LIQUIDACION, EtapaSiniestro.CERRADO, 0, "Rechazado", "Resolución registrada"
        ))
    }
}
