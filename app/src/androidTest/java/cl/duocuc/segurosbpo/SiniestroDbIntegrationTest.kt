package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.ArchivoEvidencia
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Liquidador
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SiniestroDbIntegrationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombreDb = "seguros_bpo_integracion_test.db"
    private lateinit var db: SiniestroDbHelper

    @Before
    fun preparar() {
        context.deleteDatabase(nombreDb)
        db = SiniestroDbHelper(context, nombreDb)
    }

    @After
    fun limpiar() {
        db.close()
        context.deleteDatabase(nombreDb)
    }

    private fun reporte() = FormularioUiState(
        asegurado = Asegurado.CAMILA,
        fechaOcurrencia = "20-10-2026",
        comuna = "Quilicura",
        descripcion = "Colisión ficticia de prueba",
        datosCorrectos = true
    )

    @Test
    fun registroPersisteYSoloLoConsultaSuAsegurado() {
        val anterior = db.listar(Asegurado.CAMILA).size
        val nuevo = db.registrar(reporte())
        assertTrue(nuevo.numero.matches(Regex("SIN-[0-9]{2}-[0-9]{6}")))
        assertEquals(anterior + 1, db.listar(Asegurado.CAMILA).size)
        assertNotNull(db.obtener(nuevo.id, Asegurado.CAMILA))
        assertNull(db.obtener(nuevo.id, Asegurado.JORGE))

        db.close()
        db = SiniestroDbHelper(context, nombreDb)
        assertEquals(nuevo.numero, db.obtener(nuevo.id, Asegurado.CAMILA)?.numero)
    }

    @Test
    fun correccionYDocumentoQuedanEnHistorial() {
        val nuevo = db.registrar(reporte())
        assertTrue(db.actualizarDescripcion(nuevo.id, Asegurado.CAMILA, "Descripción corregida"))
        assertFalse(db.actualizarDescripcion(nuevo.id, Asegurado.JORGE, "Intrusión"))
        db.guardarEvidencia(
            nuevo.id, Asegurado.CAMILA, "Foto de daños",
            ArchivoEvidencia("foto.jpg", "image/jpeg",
                byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()))
        )
        assertEquals(1, db.listarEvidencias(nuevo.id, Asegurado.CAMILA).size)
        assertTrue(db.listarEvidencias(nuevo.id, Asegurado.JORGE).isEmpty())
        val movimientos = db.listarGestiones(nuevo.id, Asegurado.CAMILA)
        assertTrue(movimientos.any { it.descripcion.contains("Reporte registrado") })
        assertTrue(movimientos.any { it.descripcion.contains("Descripción corregida") })
        assertTrue(movimientos.any { it.descripcion.contains("Documento adjuntado") })
        assertTrue(db.listarGestiones(nuevo.id, Asegurado.JORGE).isEmpty())
    }

    @Test
    fun gestionInternaNoSeMuestraAlAsegurado() {
        val caso = db.listar(Asegurado.JORGE).single()
        assertEquals(EtapaSiniestro.EVALUACION, caso.etapa)
        assertEquals(Liquidador.HOGAR, caso.liquidadorAsignado)
        val avisoPrivado = "Esta nota es solo para el liquidador"
        db.registrarGestionAsignada(caso.id, Liquidador.HOGAR, avisoPrivado, false)

        assertTrue(db.listarGestionesAsignadas(caso.id, Liquidador.HOGAR)
            .any { it.descripcion == avisoPrivado && !it.visibleAsegurado })
        assertFalse(db.listarGestiones(caso.id, Asegurado.JORGE)
            .any { it.descripcion == avisoPrivado })
        assertFalse(db.listarAsignados(Liquidador.VEHICULOS).any { it.id == caso.id })
    }

    @Test
    fun liquidadorAvanzaYCierraConResultadoPersistente() {
        val caso = db.listar(Asegurado.JORGE).single()
        db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        assertEquals(EtapaSiniestro.LIQUIDACION,
            db.obtener(caso.id, Asegurado.JORGE)?.etapa)
        db.avanzarAsignado(caso.id, Liquidador.HOGAR,
            "Aprobado", "Resolución ficticia finalizada")
        val final = db.obtener(caso.id, Asegurado.JORGE)
        assertEquals(EtapaSiniestro.CERRADO, final?.etapa)
        assertEquals("Aprobado", final?.resultadoCierre)
        assertEquals("Resolución ficticia finalizada", final?.mensajeCierre)
        assertTrue(db.listarGestiones(caso.id, Asegurado.JORGE)
            .any { it.descripcion.contains("Caso cerrado") })
        assertTrue(runCatching {
            db.avanzarAsignado(caso.id, Liquidador.HOGAR, "Aprobado", "Otra vez")
        }.isFailure)
    }

    @Test
    fun noSeGuardaEvidenciaEnCasoCerrado() {
        val caso = db.listar(Asegurado.HECTOR).single()
        assertEquals(EtapaSiniestro.CERRADO, caso.etapa)
        val error = runCatching {
            db.guardarEvidencia(
                caso.id, Asegurado.HECTOR, "Archivo tardío",
                ArchivoEvidencia("foto.jpg", "image/jpeg",
                    byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()))
            )
        }
        assertTrue(error.isFailure)
        assertTrue(db.listarEvidencias(caso.id, Asegurado.HECTOR).isEmpty())
    }
}
