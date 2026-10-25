package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorCuenta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorCuentaLimitesTest {
    @Test fun compruebaEstructuraRutYCorreo() {
        assertFalse(ValidadorCuenta.rutValido(""))
        assertFalse(ValidadorCuenta.rutValido("12345678"))
        assertFalse(ValidadorCuenta.rutValido("1-5"))
        assertFalse(ValidadorCuenta.rutValido("12.345.678-Z"))
        assertFalse(ValidadorCuenta.rutValido("12.345.678-6"))
        assertTrue(ValidadorCuenta.rutValido("12.345.678-5"))
        assertEquals("ejemplo@correo.cl", ValidadorCuenta.normalizarCorreo(" EJEMPLO@CORREO.CL "))
    }

    @Test fun detectaCadaCampoDeRegistro() {
        fun validar(
            nombre: String = "Ana Prueba",
            rut: String = "12.345.678-5",
            correo: String = "ana@ejemplo.cl",
            telefono: String = "912345678",
            clave: String = "Clave2026",
            confirma: String = "Clave2026"
        ) = ValidadorCuenta.errorRegistro(nombre, rut, correo, telefono, clave, confirma)

        assertEquals("Ingresa tu nombre completo", validar(nombre = "A"))
        assertEquals("Ingresa un RUT válido", validar(rut = "123"))
        assertEquals("Ingresa un correo válido", validar(correo = "incorrecto"))
        assertEquals("Ingresa un teléfono válido", validar(telefono = "12"))
        assertEquals("La contraseña debe tener al menos 8 caracteres", validar(clave = "123"))
        assertEquals("Las claves no coinciden", validar(confirma = "Distinta2026"))
        assertNull(validar())
    }
}
