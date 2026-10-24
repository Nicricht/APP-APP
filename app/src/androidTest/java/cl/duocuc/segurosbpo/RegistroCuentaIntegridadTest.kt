package cl.duocuc.segurosbpo

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.viewmodel.CuentaSesionViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistroCuentaIntegridadTest {
    private val app = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as Application
    private val nombre = "qa_registro_integro.db"
    private val correo = "qa.registro.interrumpido@ejemplo.cl"
    private val rut = "26.354.719-5"
    private val trigger = "qa_borrar_alta_en_prueba"
    private lateinit var db: CuentaDbHelper

    @Before fun preparar() {
        app.getSharedPreferences("cuenta_sesion_local", 0).edit()
            .remove("cuenta_id").commit()
        CuentaDbHelper(app).use { helper ->
            helper.writableDatabase.execSQL("DROP TRIGGER IF EXISTS $trigger")
            helper.writableDatabase.delete("cuentas", "correo = ?", arrayOf(correo))
        }
        app.deleteDatabase(nombre)
        db = CuentaDbHelper(app, nombre)
    }

    @After fun limpiar() {
        if (::db.isInitialized) db.close()
        app.deleteDatabase(nombre)
        CuentaDbHelper(app).use { helper ->
            helper.writableDatabase.execSQL("DROP TRIGGER IF EXISTS $trigger")
            helper.writableDatabase.delete("cuentas", "correo = ?", arrayOf(correo))
        }
        app.getSharedPreferences("cuenta_sesion_local", 0).edit()
            .remove("cuenta_id").commit()
    }

    private fun crearTrigger(helper: CuentaDbHelper) {
        helper.writableDatabase.execSQL("""
            CREATE TRIGGER $trigger
            AFTER INSERT ON cuentas
            WHEN NEW.correo = '$correo'
            BEGIN
                DELETE FROM cuentas WHERE id = NEW.id;
            END
        """.trimIndent())
    }

    private fun contar(helper: CuentaDbHelper): Int =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM cuentas WHERE correo = ?", arrayOf(correo)
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        }

    @Test fun sqliteNoDevuelveCuentaSiLaFilaDesapareceAlInsertarla() {
        val liquidadorOriginal = db.obtener("LIQ_HOGAR")
        crearTrigger(db)
        val fallo = runCatching {
            db.registrarAsegurado(
                "Ana Prueba", rut, correo, "912345678", "Clave2026", "Clave2026"
            )
        }.exceptionOrNull()
        assertEquals("No se encontró la cuenta", fallo?.message)
        assertEquals(0, contar(db))
        assertEquals(liquidadorOriginal, db.obtener("LIQ_HOGAR"))
        assertNull(db.iniciarSesion(correo, "Clave2026"))
    }

    @Test fun viewModelNoConcedeSesionSiSQLiteDescartaAlta() {
        CuentaDbHelper(app).use(::crearTrigger)
        val vm = CuentaSesionViewModel(app)
        assertEquals(
            "No se encontró la cuenta",
            vm.registrar(
                "Ana Prueba", rut, correo, "912345678", "Clave2026", "Clave2026"
            )
        )
        assertNull(vm.cuenta.value)
        assertNull(app.getSharedPreferences("cuenta_sesion_local", 0)
            .getString("cuenta_id", null))
        CuentaDbHelper(app).use { helper ->
            assertEquals(0, contar(helper))
            assertNull(helper.iniciarSesion(correo, "Clave2026"))
        }
        assertNull(CuentaSesionViewModel(app).cuenta.value)
    }
}
