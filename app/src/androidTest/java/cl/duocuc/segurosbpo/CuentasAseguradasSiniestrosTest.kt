package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CuentasAseguradasSiniestrosTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_cuentas_siniestros.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(archivo)
        db = SiniestroDbHelper(contexto, archivo)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    @Test fun propietarioDinamicoConservaSuNombreSinAccesoCruzado() {
        val propio = Asegurado("Ana Ejemplo", "12.345.678-5", "***.***.678-5", "USR_12345")
        val creado = db.registrar(FormularioUiState(
            asegurado = propio,
            fechaOcurrencia = "20-10-2026",
            comuna = "Santiago",
            descripcion = "Incidente ficticio de prueba",
            datosCorrectos = true
        ))
        assertEquals(creado.numero, db.obtener(creado.id, propio)?.numero)
        assertEquals(propio.etiqueta, db.listar(propio).first { it.id == creado.id }.asegurado.etiqueta)
        assertNull(db.obtener(creado.id, Asegurado.JORGE))
        assertTrue(db.listar(Asegurado.JORGE).none { it.id == creado.id })
        assertTrue(db.listarGestiones(creado.id, Asegurado.JORGE).isEmpty())
        db.close()
        db = SiniestroDbHelper(contexto, archivo)
        assertEquals(propio.name, db.listar(propio).first { it.id == creado.id }.asegurado.name)
    }

    @Test fun tresAseguradosDeDemostracionNoVenLosSiniestrosDeLosOtros() {
        val perfiles = listOf(Asegurado.RICARDO, Asegurado.FRANCISCA, Asegurado.LUIS)
        val reportes = perfiles.mapIndexed { indice, asegurado ->
            db.registrar(FormularioUiState(
                asegurado = asegurado,
                fechaOcurrencia = "20-10-2026",
                comuna = "Santiago",
                descripcion = "Incidente ficticio aislado $indice",
                datosCorrectos = true
            ))
        }

        perfiles.forEachIndexed { indice, asegurado ->
            val propio = reportes[indice]
            assertEquals(asegurado.name, db.obtener(propio.id, asegurado)?.asegurado?.name)
            assertTrue(db.listar(asegurado).any { it.id == propio.id })
            reportes.filterNot { it.id == propio.id }.forEach { ajeno ->
                assertNull(db.obtener(ajeno.id, asegurado))
                assertTrue(db.listar(asegurado).none { it.id == ajeno.id })
                assertTrue(db.listarGestiones(ajeno.id, asegurado).isEmpty())
            }
        }
    }
}
