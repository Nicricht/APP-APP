package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.PerfilesDemostracion
import cl.duocuc.segurosbpo.model.RolCuenta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfilesDemostracionTest {
    @Test fun ofreceAseguradosYLiquidadoresSinCredenciales() {
        val perfiles = PerfilesDemostracion.todos
        assertEquals(8, perfiles.count { it.rol == RolCuenta.ASEGURADO })
        assertEquals(2, perfiles.count { it.rol == RolCuenta.LIQUIDADOR })
        assertEquals(10, perfiles.map { it.id }.distinct().size)
        assertTrue(perfiles.filter { it.rol == RolCuenta.ASEGURADO }
            .all { it.equipo == null })
        assertTrue(perfiles.filter { it.rol == RolCuenta.LIQUIDADOR }
            .all { it.equipo in setOf("VEHICULOS", "HOGAR") })
        assertTrue(perfiles.all { it.correo.isEmpty() && it.telefono.isEmpty() })
    }

    @Test fun buscaUnicamentePerfilesPermitidos() {
        assertEquals("CAMILA", PerfilesDemostracion.porId("CAMILA")?.id)
        assertEquals("LIQ_HOGAR", PerfilesDemostracion.porId("LIQ_HOGAR")?.id)
        assertNull(PerfilesDemostracion.porId("INVENTADO"))
    }
}
