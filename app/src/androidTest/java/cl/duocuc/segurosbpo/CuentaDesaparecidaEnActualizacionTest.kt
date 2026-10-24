package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CuentaDesaparecidaEnActualizacionTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_cuenta_desaparecida.db"
    private lateinit var db: CuentaDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(archivo)
        db = CuentaDbHelper(contexto, archivo)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    private fun registrar() = db.registrarAsegurado(
        "Ana Prueba", "12.345.678-5", "ana@ejemplo.cl",
        "912345678", "Clave2026", "Clave2026"
    )

    @Test fun cuentaEliminadaDuranteCambioDeTelefonoNoProduceFalsoExito() {
        val cuenta = registrar()
        db.writableDatabase.execSQL("""
            CREATE TRIGGER qa_eliminar_tras_telefono
            AFTER UPDATE OF telefono ON cuentas
            WHEN NEW.id = '${cuenta.id}'
            BEGIN
                DELETE FROM cuentas WHERE id = NEW.id;
            END
        """.trimIndent())
        val error = runCatching {
            db.actualizarTelefono(cuenta.id, "988776655")
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals("No se encontró la cuenta", error?.message)
        assertNull(db.obtener(cuenta.id))
        assertNull(db.iniciarSesion("ana@ejemplo.cl", "Clave2026"))
    }

    @Test fun cuentaEliminadaDuranteCambioDeFotoNoProduceFalsoExito() {
        val cuenta = registrar()
        db.writableDatabase.execSQL("""
            CREATE TRIGGER qa_eliminar_tras_foto
            AFTER UPDATE OF foto ON cuentas
            WHEN NEW.id = '${cuenta.id}'
            BEGIN
                DELETE FROM cuentas WHERE id = NEW.id;
            END
        """.trimIndent())
        val foto = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()
        )
        val error = runCatching { db.actualizarFoto(cuenta.id, foto) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals("No se encontró la cuenta", error?.message)
        assertNull(db.obtener(cuenta.id))
        assertNull(db.iniciarSesion("ana@ejemplo.cl", "Clave2026"))
    }
}
