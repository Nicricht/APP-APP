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
class SeguimientoSemanalDbTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_seguimiento_semanal.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
        db = SiniestroDbHelper(contexto, nombre)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(nombre)
    }

    private fun fecha(dias: Long): Date {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return Date(formato.parse("01-09-2026")!!.time + TimeUnit.DAYS.toMillis(dias))
    }

    private fun prepararGestionAntigua(): Long {
        val caso = db.listar(Asegurado.JORGE).single()
        val filas = db.writableDatabase.update(
            "gestiones",
            ContentValues().apply { put("fecha", "01-09-2026 10:00") },
            "siniestro_id = ?",
            arrayOf(caso.id.toString())
        )
        assertTrue(filas > 0)
        return caso.id
    }

    @Test fun cumplePlazoExactoYNoFiltraCasosDeOtraEspecialidad() {
        val id = prepararGestionAntigua()
        assertFalse(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(6)))
        assertTrue(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(7)))
        assertTrue(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(14)))
        assertTrue(db.listarSeguimientosVencidos(Liquidador.VEHICULOS, fecha(14)).isEmpty())
    }

    @Test fun gestionInternaNoRenuevaPlazoPeroVisibleSi() {
        val id = prepararGestionAntigua()
        assertTrue(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(7)))
        db.registrarGestionAsignada(id, Liquidador.HOGAR, "Inspección interna", false)
        assertTrue(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(7)))
        db.registrarGestionAsignada(id, Liquidador.HOGAR, "Estado comunicado al asegurado", true)
        assertFalse(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(7)))
        assertEquals(1, db.listarGestionesAsignadas(id, Liquidador.HOGAR)
            .count { it.descripcion == "Estado comunicado al asegurado" && it.visibleAsegurado })
    }

    @Test fun casoCerradoYaNoSolicitaSeguimientoSemanal() {
        val id = prepararGestionAntigua()
        assertTrue(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(7)))
        db.avanzarAsignado(
            id,
            Liquidador.HOGAR,
            "Rechazado",
            "Se informó la decisión de la compañía",
            EtapaSiniestro.CERRADO
        )
        assertEquals(EtapaSiniestro.CERRADO, db.obtener(id, Asegurado.JORGE)?.etapa)
        assertFalse(id in db.listarSeguimientosVencidos(Liquidador.HOGAR, fecha(30)))
    }
}
