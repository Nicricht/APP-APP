package cl.duocuc.segurosbpo

import cl.duocuc.segurosbpo.viewmodel.BorradorEvidenciaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class BorradorEvidenciaViewModelTest {
    @Test fun nuncaGuardaSiNoHayArchivoSeleccionado() {
        val borrador = BorradorEvidenciaViewModel()
        borrador.guardar("Fotografía", { _, _ -> error("No se debe guardar") },
            { _, _ -> error("No se debe guardar") })

        assertEquals("Completa el tipo de documento y selecciona un archivo", borrador.resultado)
        assertFalse(borrador.guardando)
        assertFalse(borrador.guardadoCorrecto)
        assertNull(borrador.fotoPendiente)
        assertNull(borrador.archivoPendiente)
    }
}
