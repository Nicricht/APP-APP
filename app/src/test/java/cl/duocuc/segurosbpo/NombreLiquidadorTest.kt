package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.nombreLiquidadorPublico
import org.junit.Assert.assertEquals
import org.junit.Test

class NombreLiquidadorTest {
    @Test fun ocultaApellidosCompletos() {
        assertEquals("Paulina F.", nombreLiquidadorPublico("Paulina Fernández Ruiz"))
        assertEquals("María P.", nombreLiquidadorPublico("María P. Lagos"))
    }
}
