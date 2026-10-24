package cl.duocuc.segurosbpo

import android.app.Application
import android.content.ContentValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.viewmodel.CuentaSesionViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestauracionSesionCorruptaTest {
    private val app = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as Application
    private val preferencias by lazy {
        app.getSharedPreferences("cuenta_sesion_local", 0)
    }
    private val idPrueba = "QA_SESION_CORRUPTA"

    @Before fun preparar() {
        preferencias.edit().remove("cuenta_id").commit()
        borrarCuenta()
    }

    @After fun limpiar() {
        preferencias.edit().remove("cuenta_id").commit()
        borrarCuenta()
    }

    private fun borrarCuenta() {
        CuentaDbHelper(app).use { helper ->
            helper.writableDatabase.delete("cuentas", "id = ?", arrayOf(idPrueba))
        }
    }

    private fun cuentaLocal(rol: String, equipo: String?) {
        CuentaDbHelper(app).use { helper ->
            val datos = ContentValues().apply {
                put("id", idPrueba)
                put("nombre", "Cuenta de validación")
                put("rut", "QA-SESION-CORRUPTA")
                put("correo", "qa.corrupta@ejemplo.cl")
                put("telefono", "900000000")
                put("rol", rol)
                if (equipo == null) putNull("equipo") else put("equipo", equipo)
                put("salt", ByteArray(16) { 1 })
                put("clave_hash", ByteArray(32) { 2 })
            }
            helper.writableDatabase.insertOrThrow("cuentas", null, datos)
        }
        preferencias.edit().putString("cuenta_id", idPrueba).commit()
    }

    @Test fun rolDesconocidoNoAbreSesionNiCierraLaAplicacion() {
        cuentaLocal("ROL_DESCONOCIDO", null)
        val restaurada = CuentaSesionViewModel(app)
        assertNull(restaurada.cuenta.value)
        restaurada.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }

    @Test fun liquidadorSinEquipoNoRestauraPrivilegios() {
        cuentaLocal(RolCuenta.LIQUIDADOR.name, null)
        val restaurada = CuentaSesionViewModel(app)
        assertNull(restaurada.cuenta.value)
    }

    @Test fun liquidadorConEquipoNoReconocidoNoRestauraPrivilegios() {
        cuentaLocal(RolCuenta.LIQUIDADOR.name, "NO_EXISTE")
        val restaurada = CuentaSesionViewModel(app)
        assertNull(restaurada.cuenta.value)
    }

    @Test fun liquidadorRealValidoSigueRestaurandoSuEspecialidad() {
        CuentaDbHelper(app).use { helper ->
            assertEquals("HOGAR", helper.obtener("LIQ_HOGAR")?.equipo)
        }
        preferencias.edit().putString("cuenta_id", "LIQ_HOGAR").commit()
        val restaurada = CuentaSesionViewModel(app)
        assertEquals(RolCuenta.LIQUIDADOR, restaurada.cuenta.value?.rol)
        assertEquals("HOGAR", restaurada.cuenta.value?.equipo)
        restaurada.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }
}
