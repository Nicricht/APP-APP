package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class LimitesReglasTest {
    @Test fun formatosYFechasFueraDePlazo() {
        val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).parse("22-10-2026")!!
        assertNull(ValidadorSiniestro.errorFecha("22-10-2026", fecha))
        assertNotNull(ValidadorSiniestro.errorFecha("23-10-2026", fecha))
        assertNotNull(ValidadorSiniestro.errorFecha("31-02-2026", fecha))
        assertNotNull(ValidadorSiniestro.errorFecha("incorrecta", fecha))
        assertTrue(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "02-10-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("02-10-2026", "01-09-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("31-02-2026", "02-10-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "32-10-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "abc"))
    }

    @Test fun valoresDeFronteraParaAdjuntos() {
        val limite = ValidadorEvidencia.MAX_BYTES
        assertNotNull(ValidadorEvidencia.error("", "image/jpeg", 10, 0))
        assertNotNull(ValidadorEvidencia.error("foto", "text/plain", 10, 0))
        assertNotNull(ValidadorEvidencia.error("foto", "image/jpeg", 0, 0))
        assertNotNull(ValidadorEvidencia.error("foto", "image/jpeg", limite + 1, 0))
        assertNotNull(ValidadorEvidencia.error("foto", "image/jpeg", 1, 15))
        assertNull(ValidadorEvidencia.error("foto", "image/jpeg", limite, 14))
        assertNull(ValidadorEvidencia.error("documento", "application/pdf", 1, 0))
    }

    @Test fun todasLasTransicionesPermitidasYProhibidas() {
        val recibido = EtapaSiniestro.RECIBIDO
        val evaluacion = EtapaSiniestro.EVALUACION
        val liquidacion = EtapaSiniestro.LIQUIDACION
        val cerrado = EtapaSiniestro.CERRADO
        assertNull(ReglasEtapa.validar(recibido, evaluacion, 0, "", ""))
        assertNull(ReglasEtapa.validar(evaluacion, liquidacion, 0, "", ""))
        assertNotNull(ReglasEtapa.validar(evaluacion, liquidacion, 1, "", ""))
        assertNull(ReglasEtapa.validar(liquidacion, cerrado, 0, "Aprobado", "Listo"))
        assertNull(ReglasEtapa.validar(liquidacion, cerrado, 0, "Rechazado", "Listo"))
        assertNotNull(ReglasEtapa.validar(liquidacion, cerrado, 0, "Desistido", "Listo"))
        assertNull(ReglasEtapa.validar(recibido, cerrado, 0, "Desistido", "Renuncia"))
        assertNotNull(ReglasEtapa.validar(recibido, cerrado, 0, "Aprobado", "Invalido"))
        assertNull(ReglasEtapa.validar(evaluacion, cerrado, 0, "Rechazado", "Denegado"))
        assertNull(ReglasEtapa.validar(evaluacion, cerrado, 0, "Desistido", "Renuncia"))
        assertNotNull(ReglasEtapa.validar(evaluacion, cerrado, 0, "Aprobado", "Invalido"))
        assertNotNull(ReglasEtapa.validar(recibido, cerrado, 0, "Desistido", " "))
        assertNotNull(ReglasEtapa.validar(cerrado, evaluacion, 0, "", ""))
        assertNotNull(ReglasEtapa.validar(cerrado, cerrado, 0, "", ""))
        assertNotNull(ReglasEtapa.validar(evaluacion, recibido, 0, "", ""))
        assertEquals(evaluacion, ReglasEtapa.siguiente(recibido))
        assertEquals(liquidacion, ReglasEtapa.siguiente(evaluacion))
        assertEquals(cerrado, ReglasEtapa.siguiente(liquidacion))
        assertNull(ReglasEtapa.siguiente(cerrado))
        assertNotNull(ReglasEtapa.validarGestion(" "))
        assertNotNull(ReglasEtapa.validarGestion("X".repeat(501)))
        assertNull(ReglasEtapa.validarGestion("Revisión realizada"))
    }

    @Test fun pendienteDependeSoloDeLaFechaDeEntrega() {
        val pendiente = SolicitudDocumento(1, 2, "Constancia", "", "22-10-2026", null)
        val entregado = pendiente.copy(fechaEntrega = "23-10-2026")
        assertTrue(pendiente.pendiente)
        assertFalse(entregado.pendiente)
        assertNotNull(ValidadorSiniestro.errorCompania(Compania.PROTEGE))
        assertNull(ValidadorSiniestro.errorCompania(Compania.CORDILLERA))
    }
}
