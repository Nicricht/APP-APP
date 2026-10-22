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
class ReglasPersistenciaCoberturaTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_ramas_persistencia.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
        db = SiniestroDbHelper(contexto, nombre)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(nombre)
    }

    @Test fun registroBusquedaYCorreccionNoCruzanAsegurados() {
        val estado = FormularioUiState(
            asegurado = Asegurado.CAMILA,
            fechaOcurrencia = "20-10-2026",
            comuna = "Quilicura",
            descripcion = "Choque ficticio",
            datosCorrectos = true
        )
        val nuevo = db.registrar(estado)
        assertTrue(db.listar(Asegurado.CAMILA).any { it.id == nuevo.id })
        assertTrue(db.listar(Asegurado.CAMILA, nuevo.numero).any { it.id == nuevo.id })
        assertFalse(db.listar(Asegurado.CAMILA, "%inexistente").any { it.id == nuevo.id })
        assertNull(db.obtener(nuevo.id, Asegurado.JORGE))
        assertFalse(db.actualizarDescripcion(nuevo.id, Asegurado.CAMILA, "  "))
        assertFalse(db.actualizarDescripcion(nuevo.id, Asegurado.JORGE, "Otra persona"))
        assertTrue(db.actualizarDescripcion(nuevo.id, Asegurado.CAMILA, "Daño corregido"))
        assertEquals("Daño corregido", db.obtener(nuevo.id, Asegurado.CAMILA)?.descripcion)
    }

    @Test fun rechazaSolicitudesDuplicadasYFueraDelPerfil() {
        val caso = db.listar(Asegurado.JORGE).single()
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.HOGAR, " ", "")
        }.isFailure)
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.HOGAR, "A".repeat(101), "")
        }.isFailure)
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Constancia", "X".repeat(501))
        }.isFailure)
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.VEHICULOS, "Constancia", "")
        }.isFailure)
        db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Constancia", "Subir archivo")
        assertEquals(1, db.listarSolicitudesAsignadas(caso.id, Liquidador.HOGAR).size)
        assertTrue(db.listarSolicitudesAsignadas(caso.id, Liquidador.VEHICULOS).isEmpty())
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.HOGAR, "constancia", "")
        }.isFailure)
        assertFalse(db.marcarAvisoLeido(-1, Asegurado.JORGE))
        val aviso = db.listarAvisos(Asegurado.JORGE).single()
        assertFalse(db.marcarAvisoLeido(aviso.id, Asegurado.CAMILA))
        assertTrue(db.marcarAvisoLeido(aviso.id, Asegurado.JORGE))
    }

    @Test fun evidenciaValidaMarcaSolicitudYBloqueaCuandoFalta() {
        val caso = db.listar(Asegurado.JORGE).single()
        db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Boleta", "Subir evidencia")
        assertTrue(runCatching {
            db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        }.isFailure)
        assertTrue(runCatching {
            db.guardarEvidencia(caso.id, Asegurado.JORGE, "", ArchivoEvidencia("archivo.pdf", "application/pdf", byteArrayOf(1)))
        }.isFailure)
        assertTrue(runCatching {
            db.guardarEvidencia(caso.id, Asegurado.JORGE, "Boleta", ArchivoEvidencia("archivo.txt", "text/plain", byteArrayOf(1)))
        }.isFailure)
        db.guardarEvidencia(
            caso.id, Asegurado.JORGE, "Boleta",
            ArchivoEvidencia("boleta.pdf", "application/pdf", byteArrayOf(1, 2, 3, 4, 5))
        )
        assertFalse(db.listarSolicitudes(caso.id, Asegurado.JORGE).single().pendiente)
        db.avanzarAsignado(caso.id, Liquidador.HOGAR, "", "")
        assertEquals(EtapaSiniestro.LIQUIDACION, db.obtener(caso.id, Asegurado.JORGE)?.etapa)
        assertTrue(runCatching {
            db.solicitarDocumento(caso.id, Liquidador.HOGAR, "Boleta", "")
        }.isSuccess)
    }

    @Test fun historialYDocumentosRespetanEtapasYPermisos() {
        val cerrado = db.listar(Asegurado.HECTOR).single()
        assertFalse(db.actualizarDescripcion(cerrado.id, Asegurado.HECTOR, "Cambio tardío"))
        assertTrue(runCatching {
            db.registrarGestionAsignada(cerrado.id, Liquidador.HOGAR, "Cierre", true)
        }.isFailure)
        assertTrue(db.listarGestiones(cerrado.id, Asegurado.CAMILA).isEmpty())
        assertTrue(db.listarEvidencias(cerrado.id, Asegurado.CAMILA).isEmpty())
        assertNull(db.obtenerImagen(-1, cerrado.id, Asegurado.HECTOR))
        val caso = db.listar(Asegurado.JORGE).single()
        assertTrue(runCatching {
            db.registrarGestionAsignada(caso.id, Liquidador.HOGAR, " ", true)
        }.isFailure)
        assertTrue(runCatching {
            db.registrarGestionAsignada(caso.id, Liquidador.VEHICULOS, "Nota", true)
        }.isFailure)
        db.registrarGestionAsignada(caso.id, Liquidador.HOGAR, "Revisión", false)
        assertTrue(db.listarGestionesAsignadas(caso.id, Liquidador.HOGAR).any { !it.visibleAsegurado })
        assertTrue(db.listarGestiones(caso.id, Asegurado.JORGE).none { it.descripcion == "Revisión" })
    }
}
