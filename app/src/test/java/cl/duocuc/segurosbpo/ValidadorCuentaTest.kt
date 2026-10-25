package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorCuenta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorCuentaTest {
    @Test fun validaRutFicticioYCorreo() {
        assertTrue(ValidadorCuenta.rutValido("12.345.678-5"))
        assertFalse(ValidadorCuenta.rutValido("12.345.678-6"))
        assertEquals("ana@ejemplo.cl", ValidadorCuenta.normalizarCorreo(" ANA@EJEMPLO.CL "))
    }

    @Test fun validaCamposDeRegistro() {
        assertNull(ValidadorCuenta.errorRegistro(
            "Ana Pérez", "12.345.678-5", "ana@ejemplo.cl", "912345678", "Clave2026", "Clave2026"
        ))
        assertEquals("Las claves no coinciden", ValidadorCuenta.errorRegistro(
            "Ana Pérez", "12.345.678-5", "ana@ejemplo.cl", "912345678", "Clave2026", "otraClave"
        ))
    }
}
