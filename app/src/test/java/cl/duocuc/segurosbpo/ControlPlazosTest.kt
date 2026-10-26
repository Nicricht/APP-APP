package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.Gestion
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

    @Test fun mensajesDelAseguradoNoReinicianElPlazoDelLiquidador() {
        val hoy = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).parse("23-10-2026")!!
        val gestiones = listOf(
            Gestion(1, 10, "16-10-2026 09:00", "Liquidador", "Seguimos revisando", true, "LIQUIDADOR"),
            Gestion(2, 10, "22-10-2026 10:00", "Asegurado", "Envié un documento", true, "ASEGURADO")
        )
        assertEquals(7L, ControlPlazos.diasSinGestionVisibleLiquidador(gestiones, "08-10-2026", hoy))
    }

    @Test fun gestionInternaNoCumpleSeguimientoSemanal() {
        val hoy = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).parse("23-10-2026")!!
        val gestiones = listOf(
            Gestion(1, 10, "22-10-2026 09:00", "Liquidador", "Nota privada", false, "LIQUIDADOR")
        )
        assertEquals(15L, ControlPlazos.diasSinGestionVisibleLiquidador(gestiones, "08-10-2026", hoy))
    }
}
