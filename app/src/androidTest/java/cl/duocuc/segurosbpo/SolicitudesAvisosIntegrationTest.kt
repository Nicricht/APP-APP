package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.ArchivoEvidencia
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Liquidador
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolicitudesAvisosIntegrationTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "solicitudes_avisos_test.db"
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
    fun solicitudApareceComoPendienteYGeneraAvisoPropio() {
        val caso = db.listar(Asegurado.JORGE).single()
        db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Constancia policial", "Adjuntar PDF")
        val solicitudes = db.listarSolicitudes(caso.id, Asegurado.JORGE)
        assertEquals(1, solicitudes.size)
        assertTrue(solicitudes.single().pendiente)
        assertTrue(db.listarSolicitudes(caso.id, Asegurado.CAMILA).isEmpty())
        val avisos = db.listarAvisos(Asegurado.JORGE)
        assertEquals(1, avisos.size)
        assertFalse(avisos.single().leido)
        assertTrue(db.listarAvisos(Asegurado.CAMILA).none { it.id == avisos.single().id })
        assertTrue(db.marcarAvisoLeido(avisos.single().id, Asegurado.JORGE))
        assertTrue(db.listarAvisos(Asegurado.JORGE).single().leido)
    }

    @Test
    fun documentoPendienteImpideAvanzarHastaSuEntrega() {
        val caso = db.listar(Asegurado.JORGE).single()
        db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Constancia policial", "Adjuntar PDF")
        val avance = runCatching {
            db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        }
        assertTrue(avance.isFailure)
        db.guardarEvidencia(
            caso.id,
            Asegurado.JORGE,
            "Constancia policial",
            ArchivoEvidencia("constancia.pdf", "application/pdf", byteArrayOf(1, 2, 3, 4))
        )
        assertFalse(db.listarSolicitudes(caso.id, Asegurado.JORGE).single().pendiente)
        db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        assertEquals("LIQUIDACION", db.obtener(caso.id, Asegurado.JORGE)?.etapa?.name)
        assertTrue(db.listarAvisos(Asegurado.JORGE).size >= 2)
    }

    @Test
    fun noSePermiteSolicitarEnCasoCerradoNiParaOtroEquipo() {
        val caso = db.listar(Asegurado.HECTOR).single()
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Documento", "")
        }.isFailure)
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.VEHICULOS, "Documento", "")
        }.isFailure)
    }
}
