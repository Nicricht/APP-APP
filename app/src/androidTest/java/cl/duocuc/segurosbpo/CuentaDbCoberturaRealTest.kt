package cl.duocuc.segurosbpo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.model.RolCuenta
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CuentaDbCoberturaRealTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_cuenta_ampliada.db"
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
        "Ana Prueba", "12.345.678-5", "ana@ejemplo.cl", "912345678", "Clave2026", "Clave2026"
    )

    @Test fun validacionesYDuplicadosNoCreanCuentasParciales() {
        assertTrue(runCatching {
            db.registrarAsegurado("A", "12.345.678-5", "ana@ejemplo.cl", "912345678", "Clave2026", "Clave2026")
        }.isFailure)
        val nueva = registrar()
        assertEquals(RolCuenta.ASEGURADO, nueva.rol)
        assertEquals("Ana Prueba", nueva.nombre)
        assertTrue(runCatching {
            db.registrarAsegurado("Otra Persona", "12.345.678-5", "otro@ejemplo.cl",
                "912345678", "Clave2026", "Clave2026")
        }.isFailure)
        assertTrue(runCatching {
            db.registrarAsegurado("Otra Persona", "11.111.111-1", "ANA@EJEMPLO.CL",
                "912345678", "Clave2026", "Clave2026")
        }.isFailure)
        assertNull(db.iniciarSesion("", "Clave2026"))
        assertNull(db.iniciarSesion("ana@ejemplo.cl", ""))
        assertNull(db.iniciarSesion("desconocido@ejemplo.cl", "Clave2026"))
        assertNull(db.obtener("USR_INEXISTENTE"))
        assertEquals(nueva.id, db.iniciarSesion("ana@ejemplo.cl", "Clave2026")?.id)
    }

    @Test fun telefonoYFotoSeGuardanConLimitesYPersisten() {
        val nueva = registrar()
        assertTrue(runCatching { db.actualizarTelefono(nueva.id, "123") }.isFailure)
        assertTrue(runCatching { db.actualizarTelefono("USR_NADA", "912345678") }.isFailure)
        val cuenta = db.actualizarTelefono(nueva.id, " 988776655 ")
        assertEquals("988776655", cuenta.telefono)
        assertTrue(runCatching { db.actualizarFoto(nueva.id, byteArrayOf()) }.isFailure)
        assertTrue(runCatching { db.actualizarFoto(nueva.id, ByteArray(1024 * 1024 + 1)) }.isFailure)
        assertTrue(runCatching { db.actualizarFoto("USR_NADA", byteArrayOf(1, 2, 3)) }.isFailure)
        val jpeg = byteArrayOf(0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xd9.toByte())
        assertArrayEquals(jpeg, db.actualizarFoto(nueva.id, jpeg).foto)
        db.close()
        db = CuentaDbHelper(contexto, archivo)
        assertEquals("988776655", db.obtener(nueva.id)?.telefono)
        assertArrayEquals(jpeg, db.obtener(nueva.id)?.foto)
    }

    @Test fun migraBaseVersionUnoSinPerderRegistrosExistentes() {
        db.close()
        contexto.deleteDatabase(archivo)
        val antigua = contexto.openOrCreateDatabase(archivo, Context.MODE_PRIVATE, null)
        antigua.execSQL(
            "CREATE TABLE cuentas (" +
                "id TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, " +
                "rut TEXT NOT NULL UNIQUE COLLATE NOCASE, correo TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "telefono TEXT NOT NULL, rol TEXT NOT NULL, equipo TEXT, " +
                "salt BLOB NOT NULL, clave_hash BLOB NOT NULL)"
        )
        antigua.execSQL(
            "INSERT INTO cuentas(id,nombre,rut,correo,telefono,rol,equipo,salt,clave_hash) " +
                "VALUES('LEGADO','Cuenta heredada','11.111.111-1','legado@ejemplo.cl'," +
                "'900000000','ASEGURADO',NULL,x'0102',x'0304')"
        )
        antigua.version = 1
        antigua.close()
        db = CuentaDbHelper(contexto, archivo)
        assertEquals(3, db.readableDatabase.version)
        assertEquals("Cuenta heredada", db.obtener("LEGADO")?.nombre)
        assertNull(db.obtener("LEGADO")?.foto)
        assertEquals("VEHICULOS", db.obtener("LIQ_VEHICULOS")?.equipo)
        assertEquals("HOGAR", db.obtener("LIQ_HOGAR")?.equipo)
    }

    @Test fun migracionDesdeVersionDosConservaFotoYAgregaLiquidadores() {
        db.close()
        contexto.deleteDatabase(archivo)
        val antigua = contexto.openOrCreateDatabase(archivo, Context.MODE_PRIVATE, null)
        antigua.execSQL("""
            CREATE TABLE cuentas (
                id TEXT PRIMARY KEY NOT NULL,
                nombre TEXT NOT NULL,
                rut TEXT NOT NULL UNIQUE COLLATE NOCASE,
                correo TEXT NOT NULL UNIQUE COLLATE NOCASE,
                telefono TEXT NOT NULL,
                rol TEXT NOT NULL,
                equipo TEXT,
                salt BLOB NOT NULL,
                clave_hash BLOB NOT NULL,
                foto BLOB
            )
        """.trimIndent())
        antigua.execSQL("""
            INSERT INTO cuentas(id,nombre,rut,correo,telefono,rol,equipo,salt,clave_hash,foto)
            VALUES('LEGADO_V2','Cuenta recuperada','11.111.111-1','legacy2@ejemplo.cl',
                '988776655','ASEGURADO',NULL,x'0102',x'0304',x'ffd8ffd9')
        """.trimIndent())
        antigua.version = 2
        antigua.close()
        db = CuentaDbHelper(contexto, archivo)
        assertEquals(3, db.readableDatabase.version)
        assertEquals("Cuenta recuperada", db.obtener("LEGADO_V2")?.nombre)
        assertArrayEquals(
            byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()),
            db.obtener("LEGADO_V2")?.foto
        )
        assertEquals("VEHICULOS", db.obtener("LIQ_VEHICULOS")?.equipo)
        assertEquals("HOGAR", db.obtener("LIQ_HOGAR")?.equipo)
    }

    @Test fun migracionActualNoEliminaCuentasNiReiniciaContrasenas() {
        val registrada = registrar()
        val antes = db.obtener(registrada.id)
        val demo = db.obtener("LIQ_HOGAR")
        val sqlite = db.writableDatabase
        db.onUpgrade(sqlite, 3, 3)
        assertEquals(3, sqlite.version)
        assertEquals(antes, db.obtener(registrada.id))
        assertEquals(demo, db.obtener("LIQ_HOGAR"))
        assertEquals(registrada.id, db.iniciarSesion("ANA@EJEMPLO.CL", "Clave2026")?.id)
    }

    @Test fun limiteExactoDeTelefonoAceptaOchoCaracteresSinModificarElAnteriorSiFalla() {
        val registrada = registrar()
        assertEquals("12345678", db.actualizarTelefono(registrada.id, " 12345678 ").telefono)
        val error = runCatching {
            db.actualizarTelefono(registrada.id, "1234567")
        }.exceptionOrNull()
        assertEquals("El teléfono debe tener al menos 8 caracteres", error?.message)
        assertEquals("12345678", db.obtener(registrada.id)?.telefono)
    }
}
