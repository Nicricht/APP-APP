package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.model.RolCuenta
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CuentaDbHelperTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_cuentas_locales.db"
    private lateinit var db: CuentaDbHelper

    @Before fun iniciar() {
        contexto.deleteDatabase(archivo)
        db = CuentaDbHelper(contexto, archivo)
    }

    @After fun terminar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    @Test fun liquidadoresConCredencialesTienenSoloSuEspecialidad() {
        val vehiculos = db.iniciarSesion("vehiculos@segurosbpo.demo", "Demo2026!")
        val hogar = db.iniciarSesion("hogar@segurosbpo.demo", "Demo2026!")
        assertEquals(RolCuenta.LIQUIDADOR, vehiculos?.rol)
        assertEquals("VEHICULOS", vehiculos?.equipo)
        assertEquals(RolCuenta.LIQUIDADOR, hogar?.rol)
        assertEquals("HOGAR", hogar?.equipo)
        assertNull(db.iniciarSesion("vehiculos@segurosbpo.demo", "incorrecta"))
    }

    @Test fun cuentaNuevaSeAutenticaConSuClaveYConservaSuRol() {
        val cuenta = db.registrarAsegurado(
            "Ana Pérez", "12.345.678-5", "ana@ejemplo.cl", "912345678", "Clave2026", "Clave2026"
        )
        assertEquals(RolCuenta.ASEGURADO, cuenta.rol)
        assertEquals(cuenta.id, db.iniciarSesion(" ANA@EJEMPLO.CL ", "Clave2026")?.id)
        assertNull(db.iniciarSesion("ana@ejemplo.cl", "otraClave"))
        assertEquals(cuenta.id, db.obtener(cuenta.id)?.id)
        assertTrue(runCatching {
            db.registrarAsegurado("Ana Pérez", "12.345.678-5", "ana@ejemplo.cl",
                "912345678", "Clave2026", "Clave2026")
        }.isFailure)
        db.readableDatabase.rawQuery(
            "SELECT clave_hash FROM cuentas WHERE id = ?", arrayOf(cuenta.id)
        ).use {
            assertTrue(it.moveToFirst())
            assertTrue(it.getBlob(0).isNotEmpty())
        }
    }
}
