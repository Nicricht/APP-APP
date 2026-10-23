package cl.duocuc.segurosbpo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigracionesSQLiteCoberturaTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_migraciones_versiones.db"
    private var base: SiniestroDbHelper? = null

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
    }

    @After fun limpiar() {
        base?.close()
        contexto.deleteDatabase(nombre)
    }

    private fun migrar(version: Int): Siniestro {
        contexto.openOrCreateDatabase(nombre, Context.MODE_PRIVATE, null).use { antigua ->
            val adicionales = if (version >= 4) {
                ", liquidador TEXT, resultado_cierre TEXT, mensaje_cierre TEXT"
            } else {
                ""
            }
            antigua.execSQL("""
                CREATE TABLE siniestros (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero TEXT NOT NULL UNIQUE,
                    asegurado TEXT NOT NULL,
                    compania TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    fecha_ocurrencia TEXT NOT NULL,
                    fecha_reporte TEXT NOT NULL,
                    comuna TEXT NOT NULL,
                    descripcion TEXT NOT NULL,
                    hubo_terceros INTEGER NOT NULL,
                    etapa TEXT NOT NULL,
                    ultima_actualizacion TEXT NOT NULL
                    $adicionales
                )
            """.trimIndent())
            antigua.execSQL("""
                INSERT INTO siniestros (
                    numero, asegurado, compania, tipo, fecha_ocurrencia,
                    fecha_reporte, comuna, descripcion, hubo_terceros,
                    etapa, ultima_actualizacion
                ) VALUES (
                    'SIN-26-009001', 'JORGE', 'LITORAL', 'DANO_AGUA',
                    '18-10-2026', '20-10-2026', 'Maipú',
                    'Caso anterior a la migración', 0, 'EVALUACION',
                    '20-10-2026 10:00'
                )
            """.trimIndent())
            antigua.execSQL("""
                CREATE TABLE evidencias (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    siniestro_id INTEGER NOT NULL,
                    tipo_documento TEXT NOT NULL,
                    nombre_archivo TEXT NOT NULL,
                    mime TEXT NOT NULL,
                    tamano_bytes INTEGER NOT NULL,
                    contenido BLOB NOT NULL,
                    fecha_registro TEXT NOT NULL,
                    FOREIGN KEY(siniestro_id) REFERENCES siniestros(id) ON DELETE CASCADE
                )
            """.trimIndent())
            if (version >= 3) {
                antigua.execSQL("""
                    CREATE TABLE gestiones (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        siniestro_id INTEGER NOT NULL,
                        fecha TEXT NOT NULL,
                        responsable TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        visible_asegurado INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(siniestro_id) REFERENCES siniestros(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                antigua.execSQL("""
                    INSERT INTO gestiones (
                        siniestro_id, fecha, responsable, descripcion, visible_asegurado
                    ) VALUES (1, '20-10-2026 10:00', 'Sistema', 'Gestión anterior', 1)
                """.trimIndent())
            }
            if (version >= 4) {
                antigua.execSQL("""
                    UPDATE siniestros SET liquidador = 'HOGAR' WHERE id = 1
                """.trimIndent())
            }
            antigua.version = version
        }
        val nueva = SiniestroDbHelper(contexto, nombre)
        base = nueva
        val caso = nueva.listar(Asegurado.JORGE).single()
        assertEquals(5, nueva.readableDatabase.version)
        assertEquals("SIN-26-009001", caso.numero)
        assertEquals("Caso anterior a la migración", caso.descripcion)
        assertEquals(EtapaSiniestro.EVALUACION, caso.etapa)
        assertEquals(Liquidador.HOGAR, caso.liquidadorAsignado)
        assertEquals(caso.id, nueva.obtenerAsignado(caso.id, Liquidador.HOGAR)?.id)
        return caso
    }

    @Test fun desdeVersion2RecuperaHistorialYAsignacion() {
        val caso = migrar(2)
        val gestiones = base!!.listarGestiones(caso.id, Asegurado.JORGE)
        assertEquals(1, gestiones.size)
        assertTrue(gestiones.single().descripcion.contains("incorporado al historial"))
        assertFalse(base!!.listarGestiones(caso.id, Asegurado.CAMILA).isNotEmpty())
    }

    @Test fun desdeVersion3ConservaHistorialSinDuplicarlo() {
        val caso = migrar(3)
        val gestiones = base!!.listarGestiones(caso.id, Asegurado.JORGE)
        assertEquals(1, gestiones.size)
        assertEquals("Gestión anterior", gestiones.single().descripcion)
        assertTrue(base!!.listarGestionesAsignadas(caso.id, Liquidador.VEHICULOS).isEmpty())
    }

    @Test fun desdeVersion4HabilitaSolicitudesYAvisos() {
        val caso = migrar(4)
        val gestiones = base!!.listarGestiones(caso.id, Asegurado.JORGE)
        assertEquals(1, gestiones.size)
        assertEquals("Gestión anterior", gestiones.single().descripcion)
        base!!.solicitarDocumento(caso.id, Liquidador.HOGAR, "Boleta", "Adjuntar comprobante")
        val solicitudes = base!!.listarSolicitudes(caso.id, Asegurado.JORGE)
        assertEquals(1, solicitudes.size)
        assertTrue(solicitudes.single().pendiente)
        assertEquals(1, base!!.listarAvisos(Asegurado.JORGE).size)
        assertTrue(base!!.listarAvisos(Asegurado.CAMILA).isEmpty())
    }

    @Test fun versionActualNoRepiteMigracionesNiAlteraLasTablas() {
        val actual = SiniestroDbHelper(contexto, nombre)
        base = actual
        val sqlite = actual.writableDatabase
        val casosPrevios = actual.listar(Asegurado.CAMILA).size
        actual.onUpgrade(sqlite, 5, 5)
        assertEquals(5, sqlite.version)
        assertEquals(casosPrevios, actual.listar(Asegurado.CAMILA).size)
    }

}
