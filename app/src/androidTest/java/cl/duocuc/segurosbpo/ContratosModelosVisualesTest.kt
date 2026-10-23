package cl.duocuc.segurosbpo

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.ui.theme.AcentoOscuro
import cl.duocuc.segurosbpo.ui.theme.Dimens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContratosModelosVisualesTest {
    @Test fun datosDeAseguradoYSectorDeLiquidadorMantienenSuIdentidad() {
        assertEquals("15.842.371-5", Asegurado.CAMILA.rut)
        assertTrue(Liquidador.VEHICULOS.esVehiculo)
        assertFalse(Liquidador.HOGAR.esVehiculo)
        assertEquals("Vehículos", Liquidador.VEHICULOS.equipo)
        assertEquals("Hogar", Liquidador.HOGAR.equipo)
    }

    @Test fun archivosYGestionConservanLaRelacionConElSiniestroOriginal() {
        val evidencia = Evidencia(
            501, 73, "Fotografía", "foto.jpg", "image/jpeg", 2048, "22-10-2026"
        )
        val gestion = Gestion(
            601, 73, "22-10-2026", "Asegurado",
            "Documento guardado localmente", true
        )
        assertEquals(73L, evidencia.siniestroId)
        assertEquals(73L, gestion.siniestroId)
        assertEquals(501L, evidencia.id)
        assertEquals("Documento guardado localmente", gestion.descripcion)
    }

    @Test fun solicitudPendienteYEntregadaCompartenIdSinPerderLaFecha() {
        val pendiente = SolicitudDocumento(
            901, 73, "Constancia", "Adjuntar comprobante", "22-10-2026", null
        )
        assertEquals(901L, pendiente.id)
        assertEquals(73L, pendiente.siniestroId)
        assertNull(pendiente.fechaEntrega)
        assertTrue(pendiente.pendiente)
        val entregada = pendiente.copy(fechaEntrega = "23-10-2026")
        assertEquals(901L, entregada.id)
        assertEquals(73L, entregada.siniestroId)
        assertEquals("23-10-2026", entregada.fechaEntrega)
        assertFalse(entregada.pendiente)
    }

    @Test fun dimensionesYColoresConservanContrasteYAlturaDelBanner() {
        assertEquals(Color(0xFFE0482A), AcentoOscuro)
        assertEquals(180.dp, Dimens.altoPortada)
        assertTrue(Dimens.tamanoAvatar > Dimens.tamanoIcono)
        assertTrue(Dimens.alturaBoton > Dimens.espacioPantalla)
    }
}
