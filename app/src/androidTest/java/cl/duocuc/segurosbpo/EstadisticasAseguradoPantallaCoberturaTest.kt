package cl.duocuc.segurosbpo

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.screens.EstadisticasAseguradoScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class EstadisticasAseguradoPantallaCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun sinSiniestrosNoInventaCifrasYPermiteRegresar() {
        val regreso = AtomicBoolean(false)
        rule.setContent {
            EstadisticasAseguradoScreen(emptyList(), onVolver = { regreso.set(true) })
        }
        rule.onNodeWithText("Mis estadísticas").assertExists()
        rule.onNodeWithText("No hay siniestros para mostrar en los gráficos.").assertExists()
        rule.onNodeWithText("Volver a mis siniestros").performClick()
        assertTrue(regreso.get())
    }
}
