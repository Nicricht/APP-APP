package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.ValidadorRut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorRutTest {
    @Test fun aceptaLosRutDelCatalogoDelCaso() {
        Asegurado.entries.forEach { asegurado ->
            assertTrue(asegurado.etiqueta, ValidadorRut.valido(asegurado.rut))
            assertNull(ValidadorRut.error(asegurado.rut))
        }
    }

    @Test fun rechazaRutConVerificadorIncorrecto() {
        assertFalse(ValidadorRut.valido("15.842.371-6"))
        assertEquals("El RUT no es válido", ValidadorRut.error("15.842.371-6"))
        assertEquals("Indica el RUT", ValidadorRut.error("   "))
    }

    @Test fun rechazaRutConLetrasOdemasiadoCorto() {
        assertFalse(ValidadorRut.valido("1584A371-5"))
        assertFalse(ValidadorRut.valido("5"))
        assertFalse(ValidadorRut.valido("15842371-J"))
    }

    @Test fun calculaVerificadorIncluyendoKyCero() {
        assertEquals('5', ValidadorRut.calcularVerificador("15842371"))
        assertEquals('K', ValidadorRut.calcularVerificador("12345670"))
        assertEquals('0', ValidadorRut.calcularVerificador("12345675"))
    }

    @Test fun ocultaTodoSalvoLosCuatroUltimosCaracteres() {
        assertEquals("*****3715", ValidadorRut.ocultar("15.842.371-5"))
        assertEquals("***", ValidadorRut.ocultar("1-9"))
    }
}
