package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ComunasSugeridas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComunasSugeridasTest {
    @Test fun buscarIgnoraTildesYMayusculas() {
        assertTrue(ComunasSugeridas.buscar("NUNOA").contains("Ñuñoa"))
        assertTrue(ComunasSugeridas.buscar("vina").contains("Viña del Mar"))
    }

    @Test fun listadoIncluyeRegionesYComunasDeLaCapital() {
        assertTrue(ComunasSugeridas.todas.contains("Puente Alto"))
        assertTrue(ComunasSugeridas.todas.contains("Arica"))
        assertTrue(ComunasSugeridas.todas.contains("Puerto Montt"))
        assertEquals(ComunasSugeridas.todas.distinct(), ComunasSugeridas.todas)
    }

    @Test fun filtroVacíoMuestraOpcionesYLimitaResultados() {
        assertTrue(ComunasSugeridas.buscar("").isNotEmpty())
        assertTrue(ComunasSugeridas.buscar("").size <= 25)
        assertTrue(ComunasSugeridas.buscar("inexistente").isEmpty())
    }
}
