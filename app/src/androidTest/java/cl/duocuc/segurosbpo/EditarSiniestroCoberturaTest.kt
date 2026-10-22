package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import cl.duocuc.segurosbpo.ui.screens.EditarSiniestroScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class EditarSiniestroCoberturaTest {
    @get:Rule val rule = createComposeRule()

    private fun caso(etapa: EtapaSiniestro) = Siniestro(
        id = 73,
        numero = "SIN-26-000073",
        asegurado = Asegurado.CAMILA,
        compania = Compania.CORDILLERA,
        tipo = TipoSiniestro.CHOQUE,
        fechaOcurrencia = "20-10-2026",
        fechaReporte = "21-10-2026",
        comuna = "Santiago",
        descripcion = "Daños originales",
        huboTerceros = false,
        etapa = etapa,
        ultimaActualizacion = "21-10-2026"
    )

    private fun mostrar(caso: Siniestro?, guardado: AtomicReference<String>? = null) {
        rule.setContent {
            EditarSiniestroScreen(
                siniestro = caso,
                onVolver = {},
                onGuardar = { guardado?.set(it) }
            )
        }
    }

    @Test fun casoAjenoNoSePuedeEditar() {
        mostrar(null)
        rule.onNodeWithText("Este reporte no se puede editar").assertExists()
        rule.onNodeWithText("Guardar cambios").assertDoesNotExist()
    }

    @Test fun casoEnEvaluacionNoSePuedeEditar() {
        mostrar(caso(EtapaSiniestro.EVALUACION))
        rule.onNodeWithText("Este reporte no se puede editar").assertExists()
        rule.onNodeWithText("Guardar cambios").assertDoesNotExist()
    }

    @Test fun recibidoPermiteCorregirDescripcion() {
        val guardado = AtomicReference("")
        mostrar(caso(EtapaSiniestro.RECIBIDO), guardado)
        rule.onNodeWithText("Descripción").performTextClearance()
        rule.onNodeWithText("Guardar cambios").assertIsNotEnabled()
        rule.onNodeWithText("La descripción es obligatoria").assertExists()
        rule.onNodeWithText("Descripción").performTextInput("Descripción corregida")
        rule.onNodeWithText("Guardar cambios").performClick()
        assertEquals("Descripción corregida", guardado.get())
    }
}
