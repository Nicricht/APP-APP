package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorCuenta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorCuentaCamposTest {
    @Test fun devuelveTodosLosErroresDeRegistroSimultaneamente() {
        val errores = ValidadorCuenta.erroresRegistro(
            nombre = "A", rut = "invalido", correo = "correo-invalido",
            telefono = "123", clave = "corta", confirmacion = "otra"
        )

        assertEquals("Ingresa tu nombre completo", errores.nombre)
        assertEquals("Ingresa un RUT ficticio válido", errores.rut)
        assertEquals("Ingresa un correo válido", errores.correo)
        assertEquals("Ingresa un teléfono válido", errores.telefono)
        assertEquals("Usa una clave de demostración de al menos 8 caracteres", errores.clave)
        assertEquals("Las claves no coinciden", errores.confirmacion)
        assertEquals("Ingresa tu nombre completo", errores.primerError())
    }

    @Test fun datosCorrectosNoGeneranErrores() {
        val errores = ValidadorCuenta.erroresRegistro(
            nombre = "Ana Prueba", rut = "12.345.678-5", correo = "ana@ejemplo.cl",
            telefono = "912345678", clave = "Clave2026", confirmacion = "Clave2026"
        )
        assertNull(errores.nombre)
        assertNull(errores.rut)
        assertNull(errores.correo)
        assertNull(errores.telefono)
        assertNull(errores.clave)
        assertNull(errores.confirmacion)
        assertNull(errores.primerError())
    }

    @Test fun errorRegistroPreservaPrioridadAlCorregirNombre() {
        val error = ValidadorCuenta.errorRegistro(
            nombre = "Ana Prueba", rut = "invalido", correo = "correo-invalido",
            telefono = "123", clave = "corta", confirmacion = "otra"
        )
        assertEquals("Ingresa un RUT ficticio válido", error)
    }
}
