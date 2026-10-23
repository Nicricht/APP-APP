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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TomarCasoIntegrationTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_tomar_caso.db"
    private lateinit var db: SiniestroDbHelper

    @Before
    fun preparar() {
        contexto.deleteDatabase(nombre)
        db = SiniestroDbHelper(contexto, nombre)
    }

    @After
    fun limpiar() {
        db.close()
        contexto.deleteDatabase(nombre)
    }

    @Test
    fun tomarCasoSoloPermiteEspecialidadYRegistraMovimientos() {
        val caso = db.listar(Asegurado.CAMILA).single()
        assertEquals(EtapaSiniestro.RECIBIDO, caso.etapa)
        assertTrue(db.listarDisponibles(Liquidador.VEHICULOS).any { it.id == caso.id })
        assertFalse(db.listarDisponibles(Liquidador.HOGAR).any { it.id == caso.id })
        assertFalse(db.tomarCaso(caso.id, Liquidador.HOGAR))
        assertTrue(db.tomarCaso(caso.id, Liquidador.VEHICULOS))
        assertFalse(db.tomarCaso(caso.id, Liquidador.VEHICULOS))
        assertEquals(EtapaSiniestro.EVALUACION, db.obtener(caso.id, Asegurado.CAMILA)?.etapa)
        assertEquals(Liquidador.VEHICULOS, db.obtener(caso.id, Asegurado.CAMILA)?.liquidadorAsignado)
        assertFalse(db.listarDisponibles(Liquidador.VEHICULOS).any { it.id == caso.id })
        assertTrue(db.listarGestiones(caso.id, Asegurado.CAMILA).any {
            it.descripcion.contains("Caso asignado")
        })
        assertTrue(db.listarAvisos(Asegurado.CAMILA).any {
            it.siniestroId == caso.id && it.mensaje.contains("evaluación")
        })
    }

    @Test
    fun tomarCasoNoModificaCasosCerradosNiYaAsignados() {
        val cerrado = db.listar(Asegurado.HECTOR).single()
        val asignado = db.listar(Asegurado.JORGE).single()
        assertFalse(db.tomarCaso(cerrado.id, Liquidador.HOGAR))
        assertFalse(db.tomarCaso(asignado.id, Liquidador.HOGAR))
        assertEquals(EtapaSiniestro.CERRADO, db.obtener(cerrado.id, Asegurado.HECTOR)?.etapa)
        assertEquals(Liquidador.HOGAR, db.obtener(asignado.id, Asegurado.JORGE)?.liquidadorAsignado)
    }
}
