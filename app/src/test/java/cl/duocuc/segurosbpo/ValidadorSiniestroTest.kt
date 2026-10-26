package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ValidadorSiniestroTest {
    private val hoy = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).parse("20-10-2026")!!

    @Test fun fechaValida() {
        assertNull(ValidadorSiniestro.errorFecha("19-10-2026", hoy))
    }

    @Test fun fechaImposible() {
        assertNotNull(ValidadorSiniestro.errorFecha("31-02-2026", hoy))
    }

    @Test fun fechaFutura() {
        assertNotNull(ValidadorSiniestro.errorFecha("21-10-2026", hoy))
    }

    @Test fun companiaInactiva() {
        assertNotNull(ValidadorSiniestro.errorCompania(Compania.PROTEGE))
        assertNull(ValidadorSiniestro.errorCompania(Compania.CORDILLERA))
    }

    @Test fun companiaDebeGestionarLaLineaDelSeguro() {
        assertNotNull(ValidadorSiniestro.errorCompaniaTipo(Compania.LITORAL, TipoSiniestro.CHOQUE))
        assertNotNull(ValidadorSiniestro.errorCompaniaTipo(Compania.VALLE_CENTRAL, TipoSiniestro.INCENDIO))
        assertNull(ValidadorSiniestro.errorCompaniaTipo(Compania.LITORAL, TipoSiniestro.ROBO_HOGAR))
        assertNull(ValidadorSiniestro.errorCompaniaTipo(Compania.VALLE_CENTRAL, TipoSiniestro.CHOQUE))
        assertNull(ValidadorSiniestro.errorCompaniaTipo(Compania.CORDILLERA, TipoSiniestro.INCENDIO))
    }

    @Test fun camposObligatorios() {
        assertNotNull(ValidadorSiniestro.errorComuna("   "))
        assertNotNull(ValidadorSiniestro.errorDescripcion(""))
        assertNull(ValidadorSiniestro.errorDescripcion("Daño de vehículo"))
    }
}
