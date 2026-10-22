package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class FechasLimitesCoberturaTest {
    private val referencia = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).parse("22-10-2026")!!

    @Test fun validaFormatoFechaCalendarioYFuturo() {
        assertNotNull(ValidadorSiniestro.errorFecha("", referencia))
        assertNotNull(ValidadorSiniestro.errorFecha("2026-10-22", referencia))
        assertNotNull(ValidadorSiniestro.errorFecha("31-02-2026", referencia))
        assertNotNull(ValidadorSiniestro.errorFecha("23-10-2026", referencia))
        assertNull(ValidadorSiniestro.errorFecha("22-10-2026", referencia))
        assertNull(ValidadorSiniestro.errorFecha("29-02-2024", referencia))
    }

    @Test fun plazoDistingueTreintaDiasYFechasInvalidas() {
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "01-10-2026"))
        assertTrue(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "02-10-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "31-02-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("31-02-2026", "01-10-2026"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("01-09-2026", "2026/10/02"))
        assertFalse(ValidadorSiniestro.reporteFueraDePlazo("09/01/2026", "02-10-2026"))
    }
}
