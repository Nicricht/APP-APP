package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import cl.duocuc.segurosbpo.model.textoPublicoAsignacion
import cl.duocuc.segurosbpo.model.nombrePublicoLiquidador
import org.junit.Assert.*
import org.junit.Test

class CompatibilidadCompaniaTest {
    @Test fun litoralSoloAdmiteHogar() {
        assertNotNull(ValidadorSiniestro.errorCompania(Compania.LITORAL, TipoSiniestro.CHOQUE))
        assertNull(ValidadorSiniestro.errorCompania(Compania.LITORAL, TipoSiniestro.DANO_AGUA))
    }

    @Test fun valleCentralSoloAdmiteVehiculos() {
        assertNotNull(ValidadorSiniestro.errorCompania(Compania.VALLE_CENTRAL, TipoSiniestro.INCENDIO))
        assertNull(ValidadorSiniestro.errorCompania(Compania.VALLE_CENTRAL, TipoSiniestro.CRISTALES))
    }

    @Test fun cordilleraYPatagoniaAdmitenAmbasLineas() {
        listOf(Compania.CORDILLERA, Compania.PATAGONIA).forEach { compania ->
            TipoSiniestro.semilla.forEach { tipo ->
                assertNull(ValidadorSiniestro.errorCompania(compania, tipo))
            }
        }
    }

    @Test fun companiaInactivaSeRechazaAunqueLaLineaCorresponda() {
        assertNotNull(ValidadorSiniestro.errorCompania(Compania.PROTEGE, TipoSiniestro.DANO_AGUA))
    }

    @Test fun identidadPublicaNoExponeApellidosCompletos() {
        assertEquals("Paulina F.", nombrePublicoLiquidador("Paulina Fernández Ruiz"))
        assertEquals("María P.", nombrePublicoLiquidador("María P. Lagos"))
        assertEquals("Paulina F.", nombrePublicoLiquidador("  Paulina   Fernández Ruiz  "))
        assertEquals("Paulina", nombrePublicoLiquidador("Paulina"))
        assertEquals("", nombrePublicoLiquidador(""))
    }
    @Test fun avisosEHistorialAnterioresTambienAbrevianLaIdentidad() {
        assertEquals("Caso asignado a Paulina F., del equipo Vehículos",
            textoPublicoAsignacion("Caso asignado a Paulina Fernández Ruiz, del equipo Vehículos"))
        assertEquals("Tu caso quedó a cargo de Paulina F.",
            textoPublicoAsignacion("Tu caso quedó a cargo de Paulina Fernández Ruiz."))
        assertEquals("Caso tomado por María P., del equipo Vehículos",
            textoPublicoAsignacion("Caso tomado por María P., del equipo Vehículos"))
        assertEquals("Documento recibido", textoPublicoAsignacion("Documento recibido"))
    }
}
