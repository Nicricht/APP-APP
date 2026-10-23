package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PosibleDuplicadoTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_duplicado_siniestros.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(archivo)
        db = SiniestroDbHelper(contexto, archivo)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    @Test fun avisaSinBloquearUnSegundoCasoConfirmado() {
        val formulario = FormularioUiState(
            asegurado = Asegurado.CAMILA, fechaOcurrencia = "01-09-2026",
            comuna = "Santiago", descripcion = "Incidente ficticio",
            datosCorrectos = true
        )
        assertNull(db.buscarPosibleDuplicado(formulario))
        val primero = db.registrar(formulario)
        assertEquals(primero.numero, db.buscarPosibleDuplicado(formulario))
        val segundo = db.registrar(formulario)
        assertEquals(segundo.numero, db.buscarPosibleDuplicado(formulario))
        assertNull(db.buscarPosibleDuplicado(formulario.copy(asegurado = Asegurado.JORGE)))
    }
}
