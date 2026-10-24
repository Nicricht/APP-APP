package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BusquedaSeguraCoberturaTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_busqueda_literal.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(archivo)
        db = SiniestroDbHelper(contexto, archivo)
    }

    @After fun cerrar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    @Test fun comodinesSeInterpretanComoTextoLiteral() {
        val camila = db.listar(Asegurado.CAMILA)
        assertEquals(1, camila.size)
        assertEquals(camila.single().id, db.listar(Asegurado.CAMILA, "Quilicura").single().id)
        assertEquals(camila.single().id, db.listar(Asegurado.CAMILA, "  QUILICURA  ").single().id)
        assertTrue(db.listar(Asegurado.CAMILA, "%").isEmpty())
        assertTrue(db.listar(Asegurado.CAMILA, "_").isEmpty())
        assertTrue(db.listar(Asegurado.CAMILA, "\\").isEmpty())
        assertTrue(db.listar(Asegurado.CAMILA, "SIN-no-existe").isEmpty())
        assertTrue(db.listar(Asegurado.JORGE, "Quilicura").isEmpty())
    }

    @Test fun búsquedaFiltraPorAseguradoAunqueHayaNombresIguales() {
        val reporte = db.registrar(
            FormularioUiState(
                asegurado = Asegurado.JORGE,
                fechaOcurrencia = "20-10-2026",
                comuna = "Quilicura",
                descripcion = "Registro de prueba",
                datosCorrectos = true
            )
        )
        assertEquals(1, db.listar(Asegurado.JORGE, reporte.numero).size)
        assertEquals(1, db.listar(Asegurado.CAMILA, "Quilicura").size)
        assertEquals(1, db.listar(Asegurado.JORGE, "Quilicura").size)
        assertTrue(db.listar(Asegurado.CAMILA, reporte.numero).isEmpty())
    }
}
