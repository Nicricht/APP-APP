package cl.duocuc.segurosbpo

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SiniestrosViewModelCoberturaTest {
    private lateinit var store: ViewModelStore
    private lateinit var vm: SiniestrosViewModel

    @Before fun preparar() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        app.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
        vm = SiniestrosViewModel(app)
        store = ViewModelStore()
        store.put("cobertura", vm)
    }

    @After fun limpiar() {
        store.clear()
    }

    @Test fun busquedaCambioAseguradoYLecturasRespetanPerfil() {
        assertEquals(Asegurado.CAMILA, vm.asegurado.value)
        vm.buscar("SIN-inexistente")
        assertTrue(vm.siniestros.value.isEmpty())
        vm.seleccionarAsegurado(Asegurado.JORGE)
        assertEquals(Asegurado.JORGE, vm.asegurado.value)
        assertEquals("", vm.busqueda.value)
        assertTrue(vm.evidencias.value.isEmpty())
        assertTrue(vm.gestiones.value.isEmpty())
        assertTrue(vm.solicitudes.value.isEmpty())
        vm.refrescar()
        vm.refrescarAvisos()
        vm.refrescarEvidencias(-1)
        vm.refrescarGestiones(-1)
        assertTrue(vm.evidencias.value.isEmpty())
        assertTrue(vm.solicitudes.value.isEmpty())
        assertTrue(vm.gestiones.value.isEmpty())
        assertNull(vm.obtener(-1))
        assertFalse(vm.actualizarDescripcion(-1, "No existe"))
        assertTrue(runCatching { vm.registrar(FormularioUiState(asegurado = Asegurado.CAMILA)) }.isFailure)
    }

    @Test fun liquidadorInvalidoDevuelveErrorSinModificarDatos() {
        vm.seleccionarLiquidador(Liquidador.HOGAR)
        vm.refrescarAsignados()
        vm.cargarGestionesLiquidador(-1)
        assertTrue(vm.gestionesLiquidador.value.isEmpty())
        assertTrue(vm.solicitudesLiquidador.value.isEmpty())
        assertNotNull(vm.solicitarDocumentoLiquidador(-1, "Constancia", "Adjuntar"))
        assertNotNull(vm.registrarGestionLiquidador(-1, "Gestión", true))
        assertNotNull(vm.avanzarLiquidador(-1, "", ""))
        assertNotNull(vm.cerrarAnticipadoLiquidador(-1, "Desistido", "Motivo"))
        vm.seleccionarLiquidador(Liquidador.VEHICULOS)
        assertEquals(Liquidador.VEHICULOS, vm.liquidador.value)
    }

    @Test fun evidenciasIncorrectasDevuelvenErrorSinGuardar() = runBlocking {
        vm.marcarAvisoLeido(-1)
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            assertNotNull(vm.guardarFoto(-1, "Foto de daño", bitmap))
            assertNull(vm.cargarImagen(-1, -1))
        } finally {
            bitmap.recycle()
        }
        val uri = Uri.parse("content://documentos-inexistentes/archivo")
        assertNotNull(vm.guardarArchivo(-1, "Documento", uri))
    }
}
