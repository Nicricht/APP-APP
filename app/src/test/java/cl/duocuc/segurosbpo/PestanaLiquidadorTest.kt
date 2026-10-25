package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.ui.screens.PestanaLiquidador
import org.junit.Assert.assertEquals
import org.junit.Test

class PestanaLiquidadorTest {
    @Test fun separaResumenCasosGestionesYDocumentos() {
        assertEquals(
            listOf("Resumen", "Mis casos", "Gestiones", "Documentos"),
            PestanaLiquidador.entries.map { it.titulo }
        )
    }
}
