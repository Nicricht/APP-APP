package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormularioViewModelCoberturaTest {
    @Test fun formularioIncompletoExponeErroresYLosLimpiaAlEditar() {
        val vm = FormularioViewModel()
        assertFalse(vm.onRegistrar())
        assertNotNull(vm.uiState.value.errorFecha)
        assertNotNull(vm.uiState.value.errorComuna)
        assertNotNull(vm.uiState.value.errorDescripcion)
        assertNotNull(vm.uiState.value.errorDatos)
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Maipú")
        vm.onDescripcionCambia("Daño de prueba de cobertura")
        vm.onDatosCorrectosCambia(true)
        assertNull(vm.uiState.value.errorFecha)
        assertNull(vm.uiState.value.errorComuna)
        assertNull(vm.uiState.value.errorDescripcion)
        assertNull(vm.uiState.value.errorDatos)
        assertTrue(vm.onRegistrar())
    }

    @Test fun companiaInactivaNoPermiteRegistroHastaCorregir() {
        val vm = FormularioViewModel()
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Maipú")
        vm.onDescripcionCambia("Daño por agua en vivienda")
        vm.onDatosCorrectosCambia(true)
        vm.onCompaniaSeleccionada(Compania.PROTEGE)
        assertFalse(vm.onRegistrar())
        assertNotNull(vm.uiState.value.errorCompania)
        vm.onCompaniaSeleccionada(Compania.LITORAL)
        assertNull(vm.uiState.value.errorCompania)
        assertTrue(vm.onRegistrar())
    }

    @Test fun tipoDeCasoControlaTercerosYConfirmacionEvitaDuplicados() {
        val vm = FormularioViewModel()
        vm.onAseguradoSeleccionado(Asegurado.JORGE)
        vm.onTipoSeleccionado(TipoSiniestro.CHOQUE)
        vm.onTercerosCambia(true)
        vm.onTipoSeleccionado(TipoSiniestro.ROBO_VEHICULO)
        assertTrue(vm.uiState.value.huboTerceros)
        vm.onTipoSeleccionado(TipoSiniestro.DANO_AGUA)
        assertFalse(vm.uiState.value.huboTerceros)
        vm.onTipoSeleccionado(TipoSiniestro.CRISTALES)
        vm.onFechaCambia("20-10-2026")
        vm.onComunaCambia("Quilicura")
        vm.onDescripcionCambia("Ruptura de vidrio")
        vm.onDatosCorrectosCambia(true)
        assertTrue(vm.onRegistrar())
        vm.confirmarRegistro("SIN-26-009888")
        assertEquals("SIN-26-009888", vm.uiState.value.numeroSiniestro)
        assertTrue(vm.uiState.value.reporteExitoso)
        assertFalse(vm.onRegistrar())
        vm.onLimpiar()
        assertEquals(Asegurado.JORGE, vm.uiState.value.asegurado)
        assertFalse(vm.uiState.value.reporteExitoso)
        assertEquals("", vm.uiState.value.numeroSiniestro)
        vm.seleccionarAsegurado(Asegurado.CAMILA)
        assertEquals(Asegurado.CAMILA, vm.uiState.value.asegurado)
    }
}
