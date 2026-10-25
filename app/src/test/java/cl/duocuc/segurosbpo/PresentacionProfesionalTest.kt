package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.TextoPresentacion
import org.junit.Assert.assertEquals
import org.junit.Test

class PresentacionProfesionalTest {
    @Test fun historialAntiguoNoMuestraReferenciaAcademica() {
        assertEquals("Sistema", TextoPresentacion.responsable("Ejemplo académico"))
        assertEquals("Caso registrado en etapa: Evaluación",
            TextoPresentacion.gestion("Caso ficticio disponible en etapa: Evaluación"))
    }

    @Test fun historialRealSeConserva() {
        assertEquals("Ana Pérez", TextoPresentacion.responsable("Ana Pérez"))
        assertEquals("Se solicitó documento", TextoPresentacion.gestion("Se solicitó documento"))
    }

    @Test fun documentosDeVersionAnteriorSeMuestranConLenguajeProfesional() {
        assertEquals("Documento recibido: Cédula",
            TextoPresentacion.gestion("Documento solicitado entregado localmente: Cédula"))
    }
}
