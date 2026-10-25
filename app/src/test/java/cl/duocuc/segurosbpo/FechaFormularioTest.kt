package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.FechaFormulario
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class FechaFormularioTest {
    @Test fun fechaSeleccionadaRespetaDiaMesAnio() {
        val fecha = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.OCTOBER, 25, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals("25-10-2026", FechaFormulario.formatear(fecha.timeInMillis))
    }

    @Test fun fechaNoDesplazaDiaConZonaHorariaLocal() {
        val fecha = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.JANUARY, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertEquals("01-01-2025", FechaFormulario.formatear(fecha.timeInMillis))
    }
}
