package cl.duocuc.segurosbpo

import android.content.ContentValues
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.ArchivoEvidencia
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RecordatoriosPendientesPlazosTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_recordatorios_pendientes.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
        db = SiniestroDbHelper(contexto, nombre)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(nombre)
    }

    private fun consulta(dias: Long): Date {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
        val inicio = formato.parse("01-09-2026")!!
        return Date(inicio.time + TimeUnit.DAYS.toMillis(dias))
    }

    private fun prepararSolicitud(): Pair<Long, Long> {
        val siniestro = db.listar(Asegurado.JORGE).single()
        db.solicitarDocumento(siniestro.id, Liquidador.HOGAR, "Constancia de daños", "Adjuntar imagen o PDF")
        val solicitud = db.listarSolicitudes(siniestro.id, Asegurado.JORGE).single()
        assertEquals(1, db.writableDatabase.update(
            "solicitudes_documentos",
            ContentValues().apply { put("fecha_solicitud", "01-09-2026 09:00") },
            "id = ?", arrayOf(solicitud.id.toString())
        ))
        return siniestro.id to solicitud.id
    }

    private fun avisosRecordatorio(siniestroId: Long): List<cl.duocuc.segurosbpo.model.AvisoSiniestro> =
        db.listarAvisos(Asegurado.JORGE).filter {
            it.siniestroId == siniestroId && it.mensaje.startsWith("Recordatorio #")
        }

    @Test fun antesDeQuinceDiasNoAlertaYDesdeDiaQuinceSoloUnaVez() {
        val (siniestroId, solicitudId) = prepararSolicitud()
        val gestionesAntes = db.listarGestiones(siniestroId, Asegurado.JORGE).size
        assertEquals(0, db.generarRecordatorios(consulta(14)))
        assertTrue(avisosRecordatorio(siniestroId).isEmpty())
        assertEquals(1, db.generarRecordatorios(consulta(15)))
        assertEquals(1, avisosRecordatorio(siniestroId).size)
        assertTrue(avisosRecordatorio(siniestroId).single().mensaje.contains("Recordatorio #$solicitudId"))
        assertEquals(gestionesAntes + 1, db.listarGestiones(siniestroId, Asegurado.JORGE).size)
        assertTrue(db.listarGestiones(siniestroId, Asegurado.JORGE)
            .any { it.descripcion.contains("Recordatorio #$solicitudId") })
        assertEquals(0, db.generarRecordatorios(consulta(16)))
        assertEquals(0, db.generarRecordatorios(consulta(30)))
        assertEquals(1, avisosRecordatorio(siniestroId).size)
        assertTrue(db.listarAvisos(Asegurado.CAMILA).none { it.siniestroId == siniestroId })
    }

    @Test fun documentoEntregadoNoGeneraRecordatorio() {
        val (siniestroId, _) = prepararSolicitud()
        db.guardarEvidencia(
            siniestroId, Asegurado.JORGE, "Constancia de daños",
            ArchivoEvidencia(
                "evidencia.jpg", "image/jpeg",
                byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())
            )
        )
        assertFalse(db.listarSolicitudes(siniestroId, Asegurado.JORGE).single().pendiente)
        assertEquals(0, db.generarRecordatorios(consulta(30)))
        assertTrue(avisosRecordatorio(siniestroId).isEmpty())
    }

    @Test fun cierreAnticipadoNoEnviaRecordatoriosDeDocumentosPendientes() {
        val (siniestroId, _) = prepararSolicitud()
        assertTrue(db.listarSolicitudes(siniestroId, Asegurado.JORGE).single().pendiente)
        db.avanzarAsignado(
            siniestroId, Liquidador.HOGAR, "Desistido",
            "El caso se cerró a solicitud del asegurado",
            EtapaSiniestro.CERRADO
        )
        assertEquals(EtapaSiniestro.CERRADO, db.obtener(siniestroId, Asegurado.JORGE)?.etapa)
        assertEquals(0, db.generarRecordatorios(consulta(30)))
        assertTrue(avisosRecordatorio(siniestroId).isEmpty())
        assertTrue(db.listarAvisos(Asegurado.JORGE).any {
            it.siniestroId == siniestroId && it.mensaje.contains("Caso cerrado")
        })
    }
}
