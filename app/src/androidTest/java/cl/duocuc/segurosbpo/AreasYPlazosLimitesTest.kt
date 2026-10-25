package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.AreaHogar
import cl.duocuc.segurosbpo.model.AreaVehiculos
import cl.duocuc.segurosbpo.model.AreasSiniestro
import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.TipoSiniestro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AreasYPlazosLimitesTest {
    private fun fecha(dia: Long): Date {
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return Date(f.parse("01-09-2026")!!.time + TimeUnit.DAYS.toMillis(dia))
    }

    @Test fun cadaTipoSeClasificaSoloEnSuAreaYCoincideConEquipo() {
        assertEquals(AreaVehiculos, AreasSiniestro.paraLiquidador(Liquidador.VEHICULOS))
        assertEquals(AreaHogar, AreasSiniestro.paraLiquidador(Liquidador.HOGAR))
        val vehiculos = setOf(
            TipoSiniestro.CHOQUE,
            TipoSiniestro.ROBO_VEHICULO,
            TipoSiniestro.CRISTALES
        )
        assertEquals(TipoSiniestro.entries.size, AreaVehiculos.tipos.size + AreaHogar.tipos.size)
        TipoSiniestro.entries.forEach { tipo ->
            if (tipo in vehiculos) {
                assertTrue(AreaVehiculos.admite(tipo))
                assertFalse(AreaHogar.admite(tipo))
                assertEquals(AreaVehiculos, AreasSiniestro.paraTipo(tipo))
            } else {
                assertFalse(AreaVehiculos.admite(tipo))
                assertTrue(AreaHogar.admite(tipo))
                assertEquals(AreaHogar, AreasSiniestro.paraTipo(tipo))
            }
        }
    }

    @Test fun diasCorridosCalculaLimitesSinContarFraccionesNiFechasFuturas() {
        assertEquals(0L, ControlPlazos.diasCorridos("01-09-2026", fecha(0)))
        assertEquals(14L, ControlPlazos.diasCorridos("01-09-2026 22:59", fecha(14)))
        assertEquals(15L, ControlPlazos.diasCorridos("01-09-2026 22:59", fecha(15)))
        assertEquals(30L, ControlPlazos.diasCorridos("01-09-2026", fecha(30)))
        assertEquals(0L, ControlPlazos.diasCorridos("01-10-2026", fecha(14)))
        assertEquals(0L, ControlPlazos.diasCorridos("31-02-2026", fecha(15)))
        assertEquals(0L, ControlPlazos.diasCorridos("sin fecha", fecha(15)))
    }

    @Test fun fechaActualPorDefectoDetectaCasosAntiguosSinHoraExterna() {
        assertTrue(ControlPlazos.diasCorridos("01-01-2020") >= 2000L)
        assertEquals(0L, ControlPlazos.diasCorridos("01-01-2099"))
        assertEquals(0L, ControlPlazos.diasCorridos("fecha incompleta"))
    }

    @Test fun unRelojQueFallaNoMuestraPlazosFicticiosNiDerribaLaPantalla() {
        val relojInaccesible = object : Date(0L) {
            override fun getTime(): Long = throw IllegalStateException("Reloj no disponible")
        }

        assertEquals(0L, ControlPlazos.diasCorridos("01-09-2026", relojInaccesible))
    }
}
