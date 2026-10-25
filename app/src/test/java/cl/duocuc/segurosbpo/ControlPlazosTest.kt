package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ControlPlazos
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ControlPlazosTest {
    @Test fun calculaDiasCorridosYRechazaFechaInvalida() {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT)
        val hoy = formato.parse("23-10-2026")!!
        assertEquals(15L, ControlPlazos.diasCorridos("08-10-2026 10:00", hoy))
        assertEquals(0L, ControlPlazos.diasCorridos("invalida", hoy))
        assertEquals(0L, ControlPlazos.diasCorridos("24-10-2026", hoy))
    }
}
