package cl.duocuc.segurosbpo

import android.content.ContentValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvanceRecibidoDbCoberturaTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_avance_recibido.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
        db = SiniestroDbHelper(contexto, nombre)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(nombre)
    }

    @Test fun casoRecibidoAsignadoAvanzaYGeneraAuditoriaYAviso() {
        val caso = db.listar(Asegurado.CAMILA).single()
        assertEquals(EtapaSiniestro.RECIBIDO, caso.etapa)
        val valores = ContentValues().apply {
            put("liquidador", Liquidador.VEHICULOS.name)
        }
        assertEquals(
            1,
            db.writableDatabase.update(
                "siniestros", valores, "id = ?", arrayOf(caso.id.toString())
            )
        )
        assertTrue(runCatching {
            db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        }.isFailure)
        db.avanzarAsignado(caso.id, Liquidador.VEHICULOS, "", "")
        assertEquals(
            EtapaSiniestro.EVALUACION,
            db.obtener(caso.id, Asegurado.CAMILA)?.etapa
        )
        assertTrue(db.listarGestiones(caso.id, Asegurado.CAMILA).any {
            it.descripcion == "Etapa actualizada a En evaluación"
        })
        assertTrue(db.listarAvisos(Asegurado.CAMILA).any {
            it.siniestroId == caso.id && it.mensaje.contains("En evaluación")
        })
    }
}
