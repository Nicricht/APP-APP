package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ValidadorContacto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorContactoTest {
    @Test fun aceptaCorreosConFormatoReal() {
        assertNull(ValidadorContacto.errorCorreo("crojas@correo.cl"))
        assertNull(ValidadorContacto.errorCorreo("  Asignaciones@SegurosBpo.CL  "))
        assertNull(ValidadorContacto.errorCorreo("maria.paz+casos@seguros-bpo.co.uk"))
    }

    @Test fun rechazaCorreosMalEscritos() {
        assertNotNull(ValidadorContacto.errorCorreo("crojas@correo.clfsefe"))
        assertNotNull(ValidadorContacto.errorCorreo("crojas@correo"))
        assertNotNull(ValidadorContacto.errorCorreo("crojas correo.cl"))
        assertNotNull(ValidadorContacto.errorCorreo("@correo.cl"))
        assertNotNull(ValidadorContacto.errorCorreo("crojas@correo..cl"))
        assertEquals("Indica tu correo electrónico", ValidadorContacto.errorCorreo("  "))
    }

    @Test fun aceptaTelefonosChilenos() {
        assertNull(ValidadorContacto.errorTelefono("900000000"))
        assertNull(ValidadorContacto.errorTelefono("+56 9 1234 5678"))
        assertNull(ValidadorContacto.errorTelefono("22-345-6789"))
    }

    @Test fun rechazaTelefonosConLetrasOlargoInvalido() {
        assertEquals(
            "El teléfono solo admite números",
            ValidadorContacto.errorTelefono("900000000fsefsefsefesfe")
        )
        assertEquals(
            "El teléfono debe tener al menos 8 dígitos",
            ValidadorContacto.errorTelefono("9000")
        )
        assertEquals(
            "El teléfono no puede superar los 11 dígitos",
            ValidadorContacto.errorTelefono("900000000000")
        )
        assertEquals("Indica un teléfono de contacto", ValidadorContacto.errorTelefono(" "))
    }

    @Test fun normalizaAntesDeGuardar() {
        assertEquals("crojas@correo.cl", ValidadorContacto.normalizarCorreo(" CRojas@Correo.CL "))
        assertEquals("+56912345678", ValidadorContacto.normalizarTelefono("+56 9 1234-5678"))
    }
}
