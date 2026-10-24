package cl.duocuc.segurosbpo

import android.app.Application
import android.content.ContentValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SeguimientoSemanalViewModelTest {
    private val app = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as Application
    private val nombre = "qa_seguimiento_vm.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        app.deleteDatabase(nombre)
        db = SiniestroDbHelper(app, nombre)
    }

    @After fun limpiar() {
        db.close()
        app.deleteDatabase(nombre)
    }

    @Test fun soloUnaGestionVisibleQuitaLaAlertaDeLaBandeja() {
        val caso = db.listar(Asegurado.JORGE).single()
        db.writableDatabase.update(
            "gestiones",
            ContentValues().apply { put("fecha", "01-09-2026 09:00") },
            "siniestro_id = ?",
            arrayOf(caso.id.toString())
        )
        val vm = SiniestrosViewModel(app, db)
        vm.seleccionarLiquidador(Liquidador.HOGAR)
        assertTrue(caso.id in vm.seguimientosVencidos.value)
        assertNull(vm.registrarGestionLiquidador(caso.id, "Revisión interna", false))
        assertTrue(caso.id in vm.seguimientosVencidos.value)
        assertNull(vm.registrarGestionLiquidador(caso.id, "Sin novedades registradas", true))
        assertFalse(caso.id in vm.seguimientosVencidos.value)
        assertTrue(vm.gestionesLiquidador.value.any {
            it.descripcion == "Sin novedades registradas" && it.visibleAsegurado
        })
        vm.seleccionarLiquidador(Liquidador.VEHICULOS)
        assertTrue(vm.seguimientosVencidos.value.isEmpty())
    }
}
