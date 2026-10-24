package cl.duocuc.segurosbpo

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.viewmodel.CuentaSesionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SesionRestauracionTest {
    private val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val preferencias by lazy { app.getSharedPreferences("cuenta_sesion_local", 0) }

    @Before fun preparar() { preferencias.edit().remove("cuenta_id").commit() }

    @After fun limpiar() { preferencias.edit().remove("cuenta_id").commit() }

    @Test fun sinSesionRechazaEdicionDeTelefono() {
        val vm = CuentaSesionViewModel(app)
        assertNull(vm.cuenta.value)
        assertEquals("Inicia sesión", vm.actualizarTelefono("987654321"))
    }

    @Test fun liquidadorDemoConservaEquipoEnReinicio() {
        val vm = CuentaSesionViewModel(app)
        vm.accederLiquidadorDemo(Liquidador.HOGAR)
        assertEquals(RolCuenta.LIQUIDADOR, vm.cuenta.value?.rol)
        assertEquals("HOGAR", CuentaSesionViewModel(app).cuenta.value?.equipo)
        assertEquals("El perfil de demostración no es editable", vm.actualizarTelefono("987654321"))
        vm.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }

    @Test fun identificadorInvalidoNoRestauraPrivilegios() {
        preferencias.edit().putString("cuenta_id", "DEMO_LIQ_DESCONOCIDO").commit()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
        preferencias.edit().putString("cuenta_id", "USR_INEXISTENTE").commit()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }
}
