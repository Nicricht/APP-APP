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

    @Test fun aseguradoDemoSeRecuperaSinConvertirseEnOtraCuenta() {
        val primero = CuentaSesionViewModel(app)
        primero.accederAseguradoDemo()
        assertEquals("CAMILA", primero.cuenta.value?.id)
        assertEquals(RolCuenta.ASEGURADO, primero.cuenta.value?.rol)
        val restaurado = CuentaSesionViewModel(app)
        assertEquals("CAMILA", restaurado.cuenta.value?.id)
        assertEquals("15.842.371-5", restaurado.cuenta.value?.rut)
        assertEquals("El perfil de demostración no es editable",
            restaurado.actualizarTelefono("912345678"))
        restaurado.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }

    @Test fun vehiculosDemoSeRecuperaConRolYAreaSinCambiarDeEspecialidad() {
        val primero = CuentaSesionViewModel(app)
        primero.accederLiquidadorDemo(Liquidador.VEHICULOS)
        assertEquals("DEMO_LIQ_VEHICULOS", primero.cuenta.value?.id)
        val restaurado = CuentaSesionViewModel(app)
        assertEquals(RolCuenta.LIQUIDADOR, restaurado.cuenta.value?.rol)
        assertEquals("VEHICULOS", restaurado.cuenta.value?.equipo)
        assertEquals("DEMO_LIQ_VEHICULOS", restaurado.cuenta.value?.id)
        restaurado.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }
}
