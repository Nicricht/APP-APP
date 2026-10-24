package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.ValidadorCuenta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ValidadorCuentaRamasAndroidTest {
    @Test fun compruebaCadaRamaDeEstructuraRut() {
        assertFalse(ValidadorCuenta.rutValido(""))
        assertFalse(ValidadorCuenta.rutValido("12345678"))
        assertFalse(ValidadorCuenta.rutValido("12-345678-5"))
        assertFalse(ValidadorCuenta.rutValido("123456-5"))
        assertFalse(ValidadorCuenta.rutValido("123456789-5"))
        assertFalse(ValidadorCuenta.rutValido("12345678-Z"))
        assertFalse(ValidadorCuenta.rutValido("12345678-55"))
        assertFalse(ValidadorCuenta.rutValido("12.345.678-6"))
        assertTrue(ValidadorCuenta.rutValido("12.345.678-5"))
        assertTrue(ValidadorCuenta.rutValido("10000004-0"))
        assertTrue(ValidadorCuenta.rutValido("10000013-k"))
        assertTrue(ValidadorCuenta.rutValido("1.234.567-4"))
    }

    @Test fun validaErroresDeRegistroPorCampo() {
        fun validar(
            nombre: String = "Ana Prueba",
            rut: String = "12.345.678-5",
            correo: String = "ana@ejemplo.cl",
            telefono: String = "912345678",
            clave: String = "Clave2026",
            confirmacion: String = "Clave2026"
        ) = ValidadorCuenta.errorRegistro(nombre, rut, correo, telefono, clave, confirmacion)

        assertEquals("Ingresa tu nombre completo", validar(nombre = "  "))
        assertEquals("Ingresa un RUT ficticio válido", validar(rut = "12.345.678-6"))
        assertEquals("Ingresa un correo válido", validar(correo = "anaejemplo.cl"))
        assertEquals("Ingresa un correo válido", validar(correo = "ana @ejemplo.cl"))
        assertEquals("Ingresa un correo válido", validar(correo = "ana@ejemplo"))
        assertEquals("Ingresa un teléfono válido", validar(telefono = " 1234567 "))
        assertEquals(
            "Usa una clave de demostración de al menos 8 caracteres",
            validar(clave = "Corta1")
        )
        assertEquals("Las claves no coinciden", validar(confirmacion = "Otra2026"))
        assertNull(validar())
    }

    @Test fun admiteNormalizacionYDigitosVerificadoresEspeciales() {
        assertEquals("ana@ejemplo.cl", ValidadorCuenta.normalizarCorreo("  ANA@EJEMPLO.CL  "))
        assertNull(ValidadorCuenta.errorRegistro(
            "Ana Prueba", "10000013-k", " ANA@EJEMPLO.CL ",
            "912345678", "Clave2026", "Clave2026"
        ))
        assertNull(ValidadorCuenta.errorRegistro(
            "Ana Prueba", "10000004-0", "ana@ejemplo.cl",
            "912345678", "Clave2026", "Clave2026"
        ))
    }
}
