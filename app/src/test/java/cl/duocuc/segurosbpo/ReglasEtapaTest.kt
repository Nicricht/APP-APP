package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.ReglasEtapa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReglasEtapaTest {
    @Test fun noPermiteNuevasSolicitudesDespuesDeEvaluacion() {
        assertNull(ReglasEtapa.validarSolicitudDocumento(EtapaSiniestro.EVALUACION))
        assertNotNull(ReglasEtapa.validarSolicitudDocumento(EtapaSiniestro.LIQUIDACION))
        assertNotNull(ReglasEtapa.validarSolicitudDocumento(EtapaSiniestro.CERRADO))
    }
    @Test fun avanzaUnaEtapa() {
        assertEquals(EtapaSiniestro.LIQUIDACION,
            ReglasEtapa.siguiente(EtapaSiniestro.EVALUACION))
    }
    @Test fun impideSalto() {
        assertNotNull(ReglasEtapa.validar(EtapaSiniestro.RECIBIDO,
            EtapaSiniestro.CERRADO, 0, "Aprobado", "Cerrado"))
    }
    @Test fun impideLiquidarConPendientes() {
        assertNotNull(ReglasEtapa.validar(EtapaSiniestro.EVALUACION,
            EtapaSiniestro.LIQUIDACION, 1, "", ""))
    }
    @Test fun cierraConResultadoYMensaje() {
        assertNull(ReglasEtapa.validar(EtapaSiniestro.LIQUIDACION,
            EtapaSiniestro.CERRADO, 0, "Aprobado", "Resolución confirmada"))
    }
    @Test fun noCierraSinMensaje() {
        assertNotNull(ReglasEtapa.validar(EtapaSiniestro.LIQUIDACION,
            EtapaSiniestro.CERRADO, 0, "Aprobado", " "))
    }
    @Test fun noCierraSinResultadoValido() {
        assertNotNull(ReglasEtapa.validar(EtapaSiniestro.LIQUIDACION,
            EtapaSiniestro.CERRADO, 0, "Desistido", "Cierre"))
    }
    @Test fun puedeRechazarseEnEvaluacionConMensaje() {
        assertNull(ReglasEtapa.validar(
            EtapaSiniestro.EVALUACION, EtapaSiniestro.CERRADO,
            0, "Rechazado", "Decisión registrada"
        ))
    }
    @Test fun puedeDesistirseEnEvaluacion() {
        assertNull(ReglasEtapa.validar(
            EtapaSiniestro.EVALUACION, EtapaSiniestro.CERRADO,
            0, "Desistido", "Solicitud de desistimiento"
        ))
    }
    @Test fun recibidoSoloPermiteCierrePorDesistimiento() {
        assertNull(ReglasEtapa.validar(
            EtapaSiniestro.RECIBIDO, EtapaSiniestro.CERRADO,
            0, "Desistido", "Solicitado"
        ))
        assertNotNull(ReglasEtapa.validar(
            EtapaSiniestro.RECIBIDO, EtapaSiniestro.CERRADO,
            0, "Rechazado", "No corresponde"
        ))
    }
    @Test fun noApruebaAntesDeLiquidacion() {
        assertNotNull(ReglasEtapa.validar(
            EtapaSiniestro.EVALUACION, EtapaSiniestro.CERRADO,
            0, "Aprobado", "No corresponde"
        ))
    }
    @Test fun noReabre() {
        assertNull(ReglasEtapa.siguiente(EtapaSiniestro.CERRADO))
    }
    @Test fun gestionRequiereDescripcion() {
        assertNotNull(ReglasEtapa.validarGestion("  "))
        assertNull(ReglasEtapa.validarGestion("Revisé la documentación recibida"))
    }
}
