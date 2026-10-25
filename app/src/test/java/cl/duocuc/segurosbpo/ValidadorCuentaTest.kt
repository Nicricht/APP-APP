package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorCuenta
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorCuentaTest {
    @Test fun validaRutFicticio() {
        assertTrue(ValidadorCuenta.rutValido("12.345.678-5"))
        assertFalse(ValidadorCuenta.rutValido("12.345.678-6"))
        assertFalse(ValidadorCuenta.rutValido("12.345.678-Z"))
        assertFalse(ValidadorCuenta.rutValido(""))
    }
}
