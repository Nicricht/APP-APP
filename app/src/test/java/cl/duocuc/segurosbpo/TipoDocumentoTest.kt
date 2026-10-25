package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.TipoDocumento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TipoDocumentoTest {
    @Test fun incluyeDocumentosHabituales() {
        assertTrue("Fotografía del daño" in TipoDocumento.opciones)
        assertTrue("Padrón del vehículo" in TipoDocumento.opciones)
        assertTrue("Declaración del siniestro" in TipoDocumento.opciones)
    }

    @Test fun incorporaSolicitudesSinDuplicados() {
        val opciones = TipoDocumento.paraSolicitudes(listOf("Padrón del vehículo", "Informe técnico"))
        assertEquals(1, opciones.count { it == "Padrón del vehículo" })
        assertTrue("Informe técnico" in opciones)
    }

    @Test fun evitaOpcionesVaciasYNoRepiteDistintasMayusculas() {
        val opciones = TipoDocumento.paraSolicitudes(listOf("", "  padrón del vehículo  "))
        assertTrue(opciones.none { it.isBlank() })
        assertEquals(1, opciones.count { it.equals("Padrón del vehículo", ignoreCase = true) })
    }
}
