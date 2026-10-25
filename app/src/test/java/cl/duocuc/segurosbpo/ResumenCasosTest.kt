package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.ResumenCasos
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenCasosTest {
    private fun caso(id: Long, etapa: EtapaSiniestro) = Siniestro(
        id = id,
        numero = "SIN-26-${id.toString().padStart(6, '0')}",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Caso de prueba",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026 12:00"
    )

    @Test fun sinCasosRetornaCeros() {
        val resumen = ResumenCasos.desde(emptyList())
        assertEquals(0, resumen.total)
        assertEquals(0, resumen.abiertos)
        assertEquals(0, resumen.cerrados)
    }

    @Test fun distribucionReflejaLosCasosAsignados() {
        val resumen = ResumenCasos.desde(listOf(
            caso(1, EtapaSiniestro.RECIBIDO),
            caso(2, EtapaSiniestro.EVALUACION),
            caso(3, EtapaSiniestro.EVALUACION),
            caso(4, EtapaSiniestro.LIQUIDACION),
            caso(5, EtapaSiniestro.CERRADO)
        ))
        assertEquals(1, resumen.recibidos)
        assertEquals(2, resumen.evaluacion)
        assertEquals(1, resumen.liquidacion)
        assertEquals(1, resumen.cerrados)
        assertEquals(4, resumen.abiertos)
        assertEquals(5, resumen.total)
    }
}
