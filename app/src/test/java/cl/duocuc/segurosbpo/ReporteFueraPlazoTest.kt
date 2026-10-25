package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReporteFueraPlazoTest {
    @Test fun exactamenteTreintaDiasNoEsFueraDePlazo() {
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "01-10-2026"))
    }

    @Test fun treintaYUnDiasEsFueraDePlazo() {
        assertTrue(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "02-10-2026"))
    }

    @Test fun fechaMuyAntiguaSeMarca() {
        assertTrue(ValidadorSiniestro.reporteFueraDePlazo("02-12-2001", "22-10-2026"))
    }

    @Test fun fechaImposibleNoSeMarca() {
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("31-02-2026", "22-10-2026"))
    }

    @Test fun fechasInvalidasNoSeMarcan() {
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("966869", "22-10-2026"))
    }
}
