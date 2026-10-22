package cl.duocuc.segurosbpo

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
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
class FlujoViewModelCoberturaTest {
    private lateinit var vm: SiniestrosViewModel
    private lateinit var store: ViewModelStore

    @Before fun preparar() {
        val aplicacion = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        aplicacion.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
        vm = SiniestrosViewModel(aplicacion)
        store = ViewModelStore()
        store.put("flujo", vm)
    }

    @After fun cerrar() {
        store.clear()
        val aplicacion = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        aplicacion.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", Asegurado.CAMILA.name).commit()
    }

    @Test fun preferenciasIncorrectasRestauranAseguradoPorDefecto() {
        val aplicacion = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        aplicacion.getSharedPreferences("sesion_demo", 0).edit()
            .putString("asegurado", "DESCONOCIDO").commit()
        val vmTemporal = SiniestrosViewModel(aplicacion)
        val almacen = ViewModelStore()
        almacen.put("sesion", vmTemporal)
        try {
            assertEquals(Asegurado.CAMILA, vmTemporal.asegurado.value)
        } finally {
            almacen.clear()
        }
    }

    @Test fun registroYCorreccionActualizanEstadosReactivos() {
        vm.seleccionarAsegurado(Asegurado.CAMILA)
        val numero = vm.registrar(
            FormularioUiState(
                asegurado = Asegurado.CAMILA,
                fechaOcurrencia = "20-10-2026",
                comuna = "La Florida",
                descripcion = "Informe de prueba para cobertura",
                datosCorrectos = true
            )
        )
        val caso = vm.siniestros.value.single { it.numero == numero }
        assertEquals(EtapaSiniestro.RECIBIDO, caso.etapa)
        assertTrue(vm.actualizarDescripcion(caso.id, "Descripción corregida"))
        assertEquals("Descripción corregida", vm.obtener(caso.id)?.descripcion)
        vm.buscar(numero)
        assertEquals(1, vm.siniestros.value.size)
        vm.refrescarGestiones(caso.id)
        assertTrue(vm.gestiones.value.any { it.descripcion == "Descripción corregida" })
    }

    @Test fun fotografiaYDocumentoPdfActualizanListaDeEvidencias() = runBlocking {
        vm.seleccionarAsegurado(Asegurado.CAMILA)
        val numero = vm.registrar(
            FormularioUiState(
                asegurado = Asegurado.CAMILA,
                fechaOcurrencia = "20-10-2026",
                comuna = "Puente Alto",
                descripcion = "Fotografía y documento de prueba"
            )
        )
        val caso = vm.siniestros.value.single { it.numero == numero }
        val imagen = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        try {
            assertNull(vm.guardarFoto(caso.id, "Fotografía del daño", imagen))
            assertEquals(1, vm.evidencias.value.size)
            assertNotNull(vm.cargarImagen(caso.id, vm.evidencias.value.single().id))
        } finally {
            imagen.recycle()
        }
        val contenido = Base64.encodeToString(
            "%PDF-1.5".toByteArray(),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
        val uri = Uri.Builder()
            .scheme("content")
            .authority("cl.duocuc.segurosbpo.qa.archivos")
            .appendPath("documento")
            .appendQueryParameter("mime", "application/pdf")
            .appendQueryParameter("nombre", "constancia.pdf")
            .appendQueryParameter("contenido", contenido)
            .build()
        assertNull(vm.guardarArchivo(caso.id, "Constancia", uri))
        assertEquals(2, vm.evidencias.value.size)
        assertTrue(vm.evidencias.value.any {
            it.tipoDocumento == "Constancia" && it.tipoMime == "application/pdf"
        })
    }

    @Test fun gestionSolicitudYAvanceSonReactivos() = runBlocking {
        vm.seleccionarLiquidador(Liquidador.HOGAR)
        val caso = vm.casosLiquidador.value.first { it.asegurado == Asegurado.JORGE }
        assertEquals(EtapaSiniestro.EVALUACION, caso.etapa)
        assertNull(vm.registrarGestionLiquidador(caso.id, "Revisión interna", false))
        assertTrue(vm.gestionesLiquidador.value.any { !it.visibleAsegurado })
        assertNull(vm.solicitarDocumentoLiquidador(caso.id, "Constancia QA", "Adjuntar imagen"))
        assertTrue(vm.solicitudesLiquidador.value.any { it.pendiente })
        assertNotNull(vm.avanzarLiquidador(caso.id, "", ""))
        vm.seleccionarAsegurado(Asegurado.JORGE)
        assertTrue(vm.avisos.value.any { !it.leido })
        val foto = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        try {
            assertNull(vm.guardarFoto(caso.id, "Constancia QA", foto))
        } finally {
            foto.recycle()
        }
        vm.cargarGestionesLiquidador(caso.id)
        assertFalse(vm.solicitudesLiquidador.value.any { it.pendiente })
        assertNull(vm.avanzarLiquidador(caso.id, "", ""))
        assertEquals(EtapaSiniestro.LIQUIDACION, vm.obtener(caso.id)?.etapa)
        val aviso = vm.avisos.value.first()
        vm.marcarAvisoLeido(aviso.id)
        assertTrue(vm.avisos.value.first { it.id == aviso.id }.leido)
    }

    @Test fun cierreAnticipadoPorDesistimientoActualizaListado() {
        vm.seleccionarLiquidador(Liquidador.HOGAR)
        val caso = vm.casosLiquidador.value.first { it.asegurado == Asegurado.DANIELA }
        assertEquals(EtapaSiniestro.EVALUACION, caso.etapa)
        assertNull(vm.cerrarAnticipadoLiquidador(caso.id, "Desistido", "Solicitud registrada"))
        assertEquals(EtapaSiniestro.CERRADO, vm.casosLiquidador.value.first { it.id == caso.id }.etapa)
        vm.seleccionarAsegurado(Asegurado.DANIELA)
        assertEquals("Desistido", vm.obtener(caso.id)?.resultadoCierre)
        assertTrue(vm.avisos.value.any { it.mensaje.contains("Desistido") })
    }
}
