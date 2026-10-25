package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorEvidenciaTest {
    @Test fun aceptaJpg() =
        assertNull(ValidadorEvidencia.error("Foto del daño", "image/jpeg", 1000, 0))

    @Test fun aceptaPdfEnLimite() =
        assertNull(ValidadorEvidencia.error("Constancia", "application/pdf", 5 * 1024 * 1024, 14))

    @Test fun rechazaTipoVacio() =
        assertNotNull(ValidadorEvidencia.error(" ", "image/jpeg", 1000, 0))

    @Test fun rechazaFormatoInvalido() =
        assertNotNull(ValidadorEvidencia.error("Archivo", "application/zip", 1000, 0))

    @Test fun rechazaArchivoVacio() =
        assertNotNull(ValidadorEvidencia.error("Archivo", "image/png", 0, 0))

    @Test fun rechazaMasDeCincoMegas() =
        assertNotNull(ValidadorEvidencia.error("Foto", "image/jpeg", 5 * 1024 * 1024 + 1, 0))

    @Test fun rechazaArchivoDieciseis() =
        assertNotNull(ValidadorEvidencia.error("Foto", "image/jpeg", 100, 15))
}
