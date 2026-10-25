package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.*
import org.junit.Test

class FormularioViewModelTest {
    @Test fun formularioVacioMuestraErrores() {
        val vm = FormularioViewModel()
        assertFalse(vm.onRegistrar())
        val estado = vm.uiState.value
        assertNotNull(estado.errorFecha)
        assertNotNull(estado.errorComuna)
        assertNotNull(estado.errorDescripcion)
        assertNotNull(estado.errorDatos)
        assertNull(estado.errorCompania)
    }

    @Test fun formularioCompletoRegistraYEvitaDuplicado() {
        val vm = FormularioViewModel()
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Santiago")
        vm.onDescripcionCambia("Colisión ficticia")
        vm.onDatosCorrectosCambia(true)
        assertTrue(vm.onRegistrar())
        vm.confirmarRegistro("SIN-26-123456")
        assertEquals("SIN-26-123456", vm.uiState.value.numeroSiniestro)
        assertFalse(vm.onRegistrar())
        vm.onComunaCambia("Maipú")
        assertFalse(vm.uiState.value.reporteExitoso)
    }

    @Test fun companiaInactivaImpideRegistro() {
        val vm = FormularioViewModel()
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Maipú")
        vm.onDescripcionCambia("Daño por agua")
        vm.onDatosCorrectosCambia(true)
        vm.onCompaniaSeleccionada(Compania.PROTEGE)
        assertFalse(vm.onRegistrar())
        assertNotNull(vm.uiState.value.errorCompania)
        vm.onCompaniaSeleccionada(Compania.CORDILLERA)
        assertNull(vm.uiState.value.errorCompania)
        assertTrue(vm.onRegistrar())
    }

    @Test fun cambiarTipoRestableceTercerosSoloEnHogar() {
        val vm = FormularioViewModel()
        vm.onTipoSeleccionado(TipoSiniestro.CHOQUE)
        vm.onTercerosCambia(true)
        vm.onTipoSeleccionado(TipoSiniestro.ROBO_VEHICULO)
        assertTrue(vm.uiState.value.huboTerceros)
        vm.onTipoSeleccionado(TipoSiniestro.DANO_AGUA)
        assertFalse(vm.uiState.value.huboTerceros)
        vm.onTipoSeleccionado(TipoSiniestro.CRISTALES)
        assertFalse(vm.uiState.value.huboTerceros)
    }

    @Test fun limpiarMantieneAlAsegurado() {
        val vm = FormularioViewModel()
        vm.onAseguradoSeleccionado(Asegurado.JORGE)
        vm.onComunaCambia("Renca")
        vm.onLimpiar()
        assertEquals(Asegurado.JORGE, vm.uiState.value.asegurado)
        assertEquals("", vm.uiState.value.comuna)
        vm.seleccionarAsegurado(Asegurado.CAMILA)
        assertEquals(Asegurado.CAMILA, vm.uiState.value.asegurado)
    }
}
