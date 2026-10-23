package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EstadisticasCasos
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EstadisticasCasosTest {
    private fun caso(id: Long, tipo: TipoSiniestro, fecha: String) = Siniestro(
        id, "SIN-26-0000"+id, Asegurado.CAMILA, Compania.CORDILLERA,
        tipo, fecha, fecha, "Santiago", "Prueba", false,
        EtapaSiniestro.RECIBIDO, fecha
    )

    @Test fun agregaTiposYMesesOrdenados() {
        val casos = listOf(
            caso(1, TipoSiniestro.CHOQUE, "02-10-2026"),
            caso(2, TipoSiniestro.CHOQUE, "03-10-2026"),
            caso(3, TipoSiniestro.INCENDIO, "01-09-2026")
        )
        assertEquals(2, EstadisticasCasos.porTipo(casos).first().cantidad)
        val meses = EstadisticasCasos.porMes(casos)
        assertEquals("09/2026", meses.first().etiqueta)
        assertEquals("10/2026", meses.last().etiqueta)
        assertEquals(2, meses.last().cantidad)
        assertTrue(EstadisticasCasos.porMes(emptyList()).isEmpty())
    }
}
