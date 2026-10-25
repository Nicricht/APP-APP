package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorBorradorEvidencia
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BorradorEvidenciaTest {
    @Test fun requiereArchivoYTipoAntesDeGuardar() {
        assertFalse(ValidadorBorradorEvidencia.puedeGuardar("Foto", false, 2))
        assertFalse(ValidadorBorradorEvidencia.puedeGuardar(" ", true, 2))
        assertTrue(ValidadorBorradorEvidencia.puedeGuardar("Fotografía del daño", true, 2))
    }

    @Test fun noSuperaLimiteDeQuinceArchivos() {
        assertTrue(ValidadorBorradorEvidencia.puedeGuardar("PDF", true, 14))
        assertFalse(ValidadorBorradorEvidencia.puedeGuardar("PDF", true, 15))
    }
}
