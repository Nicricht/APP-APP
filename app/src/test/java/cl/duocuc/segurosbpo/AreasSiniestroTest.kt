package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.AreaHogar
import cl.duocuc.segurosbpo.model.AreaVehiculos
import cl.duocuc.segurosbpo.model.AreasSiniestro
import cl.duocuc.segurosbpo.model.ClasificadorArea
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.TipoSiniestro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AreasSiniestroTest {
    @Test fun elPolimorfismoRestringeTiposDeCadaEspecialidad() {
        val areas: List<ClasificadorArea> = listOf(AreaVehiculos, AreaHogar)
        assertTrue(areas[0].admite(TipoSiniestro.CHOQUE))
        assertFalse(areas[0].admite(TipoSiniestro.INCENDIO))
        assertTrue(areas[1].admite(TipoSiniestro.INCENDIO))
        assertFalse(areas[1].admite(TipoSiniestro.CHOQUE))
        assertEquals(AreaVehiculos, AreasSiniestro.paraLiquidador(Liquidador.VEHICULOS))
        assertEquals(AreaHogar, AreasSiniestro.paraLiquidador(Liquidador.HOGAR))
        assertEquals(AreaHogar, AreasSiniestro.paraTipo(TipoSiniestro.DANO_AGUA))
    }
}
