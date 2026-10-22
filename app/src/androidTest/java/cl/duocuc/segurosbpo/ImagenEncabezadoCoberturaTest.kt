package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.components.ImagenEncabezado
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagenEncabezadoCoberturaTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun imagenYTitulosSonAccesiblesEnLaCabecera() {
        composeRule.setContent {
            SegurosBpoTheme {
                ImagenEncabezado(
                    imagen = R.drawable.banner_registro,
                    descripcion = "Imagen ilustrativa del registro",
                    titulo = "Informa tu siniestro",
                    subtitulo = "Completa los datos del caso"
                )
            }
        }
        composeRule.onNodeWithText("Informa tu siniestro").assertExists()
        composeRule.onNodeWithText("Completa los datos del caso").assertExists()
        composeRule.onNodeWithContentDescription("Imagen ilustrativa del registro").assertExists()
    }
}
