package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RegistroSiniestroValidacionDbTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val archivo = "qa_registro_validacion_db.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(archivo)
        db = SiniestroDbHelper(contexto, archivo)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(archivo)
    }

    private fun formulario() = FormularioUiState(
        asegurado = Asegurado.CAMILA,
        fechaOcurrencia = "20-10-2026",
        comuna = "Santiago",
        descripcion = "Siniestro ficticio verificado desde SQLite",
        compania = Compania.CORDILLERA,
        datosCorrectos = true
    )

    private fun contar(tabla: String): Long =
        db.readableDatabase.rawQuery("SELECT COUNT(*) FROM $tabla", null).use { c ->
            c.moveToFirst()
            c.getLong(0)
        }

    @Test fun registroDirectoRechazaCamposInvalidosSinCrearDatos() {
        val fechaFutura = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT)
            .format(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5)))
        val base = formulario()
        val invalidos = listOf(
            base.copy(fechaOcurrencia = "") to "Escribe una fecha válida",
            base.copy(fechaOcurrencia = "31-02-2026") to "Escribe una fecha válida",
            base.copy(fechaOcurrencia = fechaFutura) to "La fecha no puede ser futura",
            base.copy(comuna = "  ") to "Indica la comuna",
            base.copy(descripcion = "\t") to "Cuéntanos qué pasó",
            base.copy(compania = Compania.PROTEGE) to "No se pueden registrar casos nuevos",
            base.copy(datosCorrectos = false) to "Debes confirmar que los datos"
        )
        val conteos = listOf("siniestros", "gestiones", "avisos").map(::contar)

        invalidos.forEach { (datos, mensaje) ->
            val error = runCatching { db.registrar(datos) }.exceptionOrNull()
            assertNotNull("Se aceptó un formulario inválido: $mensaje", error)
            assertTrue(error?.message.orEmpty().contains(mensaje))
            assertEquals(conteos, listOf("siniestros", "gestiones", "avisos").map(::contar))
        }
    }

    @Test fun registroValidoNormalizaTextosYQuedaAuditable() {
        val estado = formulario().copy(
            comuna = "  Santiago  ",
            descripcion = "  Reporte de prueba  "
        )
        val siniestrosAntes = contar("siniestros")
        val gestionesAntes = contar("gestiones")
        val registrado = db.registrar(estado)
        assertTrue(registrado.numero.matches(Regex("SIN-[0-9]{2}-[0-9]{6}")))
        assertEquals(EtapaSiniestro.RECIBIDO, registrado.etapa)
        assertEquals("Santiago", registrado.comuna)
        assertEquals("Reporte de prueba", registrado.descripcion)
        assertEquals(siniestrosAntes + 1, contar("siniestros"))
        assertEquals(gestionesAntes + 1, contar("gestiones"))
        assertNotNull(db.obtener(registrado.id, Asegurado.CAMILA))
        assertTrue(db.listarGestiones(registrado.id, Asegurado.CAMILA)
            .any { it.descripcion.contains("Reporte registrado") })
        assertTrue(db.listarDisponibles(cl.duocuc.segurosbpo.model.Liquidador.VEHICULOS)
            .any { it.id == registrado.id })
    }

    @Test fun reporteAntiguoSeRegistraPeroSeAdvierteFueraDePlazo() {
        val antiguo = formulario().copy(fechaOcurrencia = "01-01-2001")
        val siniestro = db.registrar(antiguo)
        assertNotNull(db.obtener(siniestro.id, Asegurado.CAMILA))
        val hoy = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).format(Date())
        assertTrue(ValidadorSiniestro.reporteFueraDePlazo(antiguo.fechaOcurrencia, hoy))
        assertFalse(siniestro.numero.isBlank())
    }
}
