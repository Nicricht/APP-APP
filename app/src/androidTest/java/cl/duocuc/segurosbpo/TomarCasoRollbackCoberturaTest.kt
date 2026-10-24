package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TomarCasoRollbackCoberturaTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_tomar_caso_rollback.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(archivo)
        db = SiniestroDbHelper(contexto, archivo)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    @Test fun errorAlInsertarAvisoRevierteAsignacionYHistorial() {
        val caso = db.listar(Asegurado.CAMILA).single()
        val gestionesAntes = db.listarGestiones(caso.id, Asegurado.CAMILA).size
        val avisosAntes = db.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == caso.id }

        db.writableDatabase.execSQL("""
            CREATE TRIGGER qa_bloquear_aviso
            BEFORE INSERT ON avisos
            WHEN NEW.siniestro_id = ${caso.id}
            BEGIN
                SELECT RAISE(ABORT, 'Error controlado al crear aviso');
            END
        """.trimIndent())

        assertTrue(runCatching {
            db.tomarCaso(caso.id, Liquidador.VEHICULOS)
        }.isFailure)
        val recuperado = db.obtener(caso.id, Asegurado.CAMILA)
        assertEquals(EtapaSiniestro.RECIBIDO, recuperado?.etapa)
        assertNull(recuperado?.liquidadorAsignado)
        assertEquals(gestionesAntes, db.listarGestiones(caso.id, Asegurado.CAMILA).size)
        assertEquals(avisosAntes, db.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == caso.id })
        assertTrue(db.listarDisponibles(Liquidador.VEHICULOS).any { it.id == caso.id })

        db.writableDatabase.execSQL("DROP TRIGGER qa_bloquear_aviso")
        assertTrue(db.tomarCaso(caso.id, Liquidador.VEHICULOS))
        assertEquals(EtapaSiniestro.EVALUACION, db.obtener(caso.id, Asegurado.CAMILA)?.etapa)
        assertEquals(gestionesAntes + 1, db.listarGestiones(caso.id, Asegurado.CAMILA).size)
        assertEquals(avisosAntes + 1, db.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == caso.id })
    }

    @Test fun dosConexionesNoAsignanDosVecesNiRepitenAvisos() {
        val caso = db.listar(Asegurado.CAMILA).single()
        val otroAcceso = SiniestroDbHelper(contexto, archivo)
        try {
            val gestionesAntes = db.listarGestiones(caso.id, Asegurado.CAMILA).size
            val avisosAntes = db.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == caso.id }
            assertTrue(db.tomarCaso(caso.id, Liquidador.VEHICULOS))
            assertFalse(otroAcceso.tomarCaso(caso.id, Liquidador.VEHICULOS))
            assertFalse(otroAcceso.tomarCaso(caso.id, Liquidador.HOGAR))
            assertEquals(EtapaSiniestro.EVALUACION, otroAcceso.obtener(caso.id, Asegurado.CAMILA)?.etapa)
            assertEquals(gestionesAntes + 1, db.listarGestiones(caso.id, Asegurado.CAMILA).size)
            assertEquals(avisosAntes + 1, db.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == caso.id })
        } finally {
            otroAcceso.close()
        }
    }
}
