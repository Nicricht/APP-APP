package cl.duocuc.segurosbpo

import android.app.Application
import android.graphics.Bitmap
import java.util.Random
import org.junit.Assert.assertArrayEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.viewmodel.CuentaSesionViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SesionCuentaRealCoberturaTest {
    private val app = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as Application
    private val correo = "qa.cuenta.real@ejemplo.cl"
    private val rut = "26.354.719-5"

    private fun limpiarDatos() {
        app.getSharedPreferences("cuenta_sesion_local", 0).edit()
            .remove("cuenta_id").commit()
        CuentaDbHelper(app).use { helper ->
            helper.writableDatabase.delete("cuentas", "correo = ?", arrayOf(correo))
        }
    }

    @Before fun preparar() {
        limpiarDatos()
    }

    @After fun limpiar() {
        limpiarDatos()
    }

    @Test fun registraRestauraYActualizaTelefonoYFoto() {
        val sesion = CuentaSesionViewModel(app)
        assertEquals("Ingresa tu nombre completo", sesion.registrar(
            "A", rut, correo, "912345678", "Clave2026", "Clave2026"
        ))
        assertNull(sesion.cuenta.value)
        assertNull(sesion.registrar(
            "Ana Prueba", rut, correo, "912345678", "Clave2026", "Clave2026"
        ))
        val id = sesion.cuenta.value?.id
        assertNotNull(id)
        assertEquals(RolCuenta.ASEGURADO, sesion.cuenta.value?.rol)
        assertEquals("El teléfono debe tener al menos 8 caracteres",
            sesion.actualizarTelefono("12"))
        assertNull(sesion.actualizarTelefono("987654321"))
        assertEquals("987654321", sesion.cuenta.value?.telefono)
        val imagen = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        assertNull(sesion.actualizarFoto(imagen))
        imagen.recycle()
        assertTrue(sesion.cuenta.value?.foto?.isNotEmpty() == true)
        val restaurada = CuentaSesionViewModel(app)
        assertEquals(id, restaurada.cuenta.value?.id)
        assertEquals("987654321", restaurada.cuenta.value?.telefono)
        assertTrue(restaurada.cuenta.value?.foto?.isNotEmpty() == true)
        restaurada.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }

    @Test fun loginIncorrectoYDuplicadosNoConcedenAcceso() {
        val sesion = CuentaSesionViewModel(app)
        assertEquals("Correo o contraseña incorrectos",
            sesion.iniciar(correo, "Incorrecta"))
        assertNull(sesion.cuenta.value)
        assertNull(sesion.registrar(
            "Ana Prueba", rut, correo, "912345678", "Clave2026", "Clave2026"
        ))
        sesion.cerrarSesion()
        val duplicado = sesion.registrar(
            "Ana Prueba", rut, correo, "912345678", "Clave2026", "Clave2026"
        )
        assertTrue(duplicado?.contains("Ya existe una cuenta") == true)
        assertNull(sesion.cuenta.value)
        assertEquals("Correo o contraseña incorrectos",
            sesion.iniciar(correo, "Incorrecta"))
        assertNull(sesion.iniciar("QA.CUENTA.REAL@EJEMPLO.CL", "Clave2026"))
        assertEquals(RolCuenta.ASEGURADO, sesion.cuenta.value?.rol)
        sesion.cerrarSesion()
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }

    @Test fun fotoDePerfilRequiereSesionRealEditable() {
        val sesion = CuentaSesionViewModel(app)
        val foto = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        try {
            assertEquals("Inicia sesión", sesion.actualizarFoto(foto))
            sesion.accederAseguradoDemo()
            assertEquals("El perfil de demostración no es editable", sesion.actualizarFoto(foto))
            sesion.accederLiquidadorDemo(cl.duocuc.segurosbpo.model.Liquidador.HOGAR)
            assertEquals("El perfil de demostración no es editable", sesion.actualizarFoto(foto))
            sesion.cerrarSesion()
            assertNull(sesion.cuenta.value)
        } finally {
            foto.recycle()
        }
    }

    @Test fun fotoGrandeRechazadaConservaLaImagenGuardada() {
        val sesion = CuentaSesionViewModel(app)
        assertNull(sesion.registrar(
            "Ana Prueba", rut, correo, "912345678", "Clave2026", "Clave2026"
        ))
        val pequena = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888)
        try {
            assertNull(sesion.actualizarFoto(pequena))
        } finally {
            pequena.recycle()
        }
        val anterior = sesion.cuenta.value!!.foto!!.clone()
        val grande = Bitmap.createBitmap(1800, 1800, Bitmap.Config.ARGB_8888)
        val generador = Random(2026L)
        val pixeles = IntArray(1800)
        try {
            for (fila in 0 until 1800) {
                for (i in pixeles.indices) {
                    pixeles[i] = generador.nextInt() or -0x1000000
                }
                grande.setPixels(pixeles, 0, 1800, 0, fila, 1800, 1)
            }
            assertEquals("La foto no puede superar 1 MB", sesion.actualizarFoto(grande))
            assertArrayEquals(anterior, sesion.cuenta.value!!.foto)
            CuentaDbHelper(app).use { helper ->
                assertArrayEquals(anterior, helper.obtener(sesion.cuenta.value!!.id)!!.foto)
            }
        } finally {
            grande.recycle()
        }
    }
}
