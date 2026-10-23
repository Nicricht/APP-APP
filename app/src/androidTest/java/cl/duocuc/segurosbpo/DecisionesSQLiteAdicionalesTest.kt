package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.TipoSiniestro
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DecisionesSQLiteAdicionalesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "qa_decisiones_sqlite.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        context.deleteDatabase(databaseName)
        db = SiniestroDbHelper(context, databaseName)
    }

    @After fun limpiar() {
        db.close()
        context.deleteDatabase(databaseName)
    }

    @Test fun participacionDeTercerosSeConservaAlGuardarYLeerElRegistro() {
        val nuevo = db.registrar(
            FormularioUiState(
                asegurado = Asegurado.CAMILA,
                tipoSiniestro = TipoSiniestro.CHOQUE,
                huboTerceros = true,
                fechaOcurrencia = "21-10-2026",
                comuna = "Santiago",
                descripcion = "Reporte con terceros"
            )
        )
        assertTrue(nuevo.huboTerceros)
        assertTrue(db.obtener(nuevo.id, Asegurado.CAMILA)!!.huboTerceros)
        assertFalse(db.obtener(nuevo.id, Asegurado.JORGE)?.huboTerceros ?: false)
        assertEquals(nuevo.id, db.listar(Asegurado.CAMILA, nuevo.numero).first().id)
    }

    @Test fun consultaAsignadaDistingueEquipoCorrespondienteYAjenos() {
        val caso = db.listar(Asegurado.JORGE).single()
        val correcto = db.obtenerAsignado(caso.id, Liquidador.HOGAR)
        assertNotNull(correcto)
        assertEquals(caso.numero, correcto!!.numero)
        assertNull(db.obtenerAsignado(caso.id, Liquidador.VEHICULOS))
        assertNull(db.obtenerAsignado(-1L, Liquidador.HOGAR))
    }

    @Test fun rechazoDeActualizacionEnSQLiteMantieneEtapaYEvitaGestionesParciales() {
        val caso = db.listar(Asegurado.JORGE).single()
        val gestionesAntes = db.listarGestionesAsignadas(caso.id, Liquidador.HOGAR).size
        db.writableDatabase.execSQL("""
            CREATE TRIGGER qa_bloquear_avance BEFORE UPDATE OF etapa ON siniestros
            WHEN OLD.id = ${caso.id}
            BEGIN
                SELECT RAISE(IGNORE);
            END
        """.trimIndent())
        val resultado = runCatching {
            db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        }
        assertTrue(resultado.isFailure)
        assertEquals(
            "El estado del siniestro cambió. Actualiza la pantalla",
            resultado.exceptionOrNull()?.message
        )
        assertEquals(caso.etapa, db.obtener(caso.id, Asegurado.JORGE)?.etapa)
        assertEquals(gestionesAntes, db.listarGestionesAsignadas(caso.id, Liquidador.HOGAR).size)
    }
}
