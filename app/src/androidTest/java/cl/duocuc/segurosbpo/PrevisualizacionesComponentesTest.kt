package cl.duocuc.segurosbpo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.components.AvatarCircularPreview
import cl.duocuc.segurosbpo.ui.components.BotonPrincipalPreview
import cl.duocuc.segurosbpo.ui.components.BotonSecundarioPreview
import cl.duocuc.segurosbpo.ui.components.CampoContrasenaPreview
import cl.duocuc.segurosbpo.ui.components.CampoTextoPreview
import cl.duocuc.segurosbpo.ui.components.ControlesSeleccionPreview
import cl.duocuc.segurosbpo.ui.components.EtiquetaInteresPreview
import cl.duocuc.segurosbpo.ui.components.ImagenEncabezadoPreview
import cl.duocuc.segurosbpo.ui.components.TarjetaEstadisticaPreview
import cl.duocuc.segurosbpo.ui.components.TarjetaSeccionPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrevisualizacionesComponentesTest {
    @get:Rule val rule = createComposeRule()

    @Test fun controlesSeleccionYCamposRenderizanEnPreview() {
        rule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ControlesSeleccionPreview()
                CampoTextoPreview()
                CampoContrasenaPreview()
            }
        }
        rule.onNodeWithText("Acepto los términos").assertExists()
        rule.onNodeWithText("Recibir novedades").assertExists()
        rule.onNodeWithText("Uno").assertExists()
        rule.onNodeWithText("Correo electrónico").assertExists()
        rule.onNodeWithText("Contraseña").assertExists()
    }

    @Test fun botonesYTarjetasRenderizanEnPreview() {
        rule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                BotonPrincipalPreview()
                BotonSecundarioPreview()
                TarjetaEstadisticaPreview()
                TarjetaSeccionPreview()
            }
        }
        rule.onNodeWithText("Registrarme").assertExists()
        rule.onNodeWithText("Con ícono").assertExists()
        rule.onNodeWithText("Deshabilitado").assertExists()
        rule.onNodeWithText("Limpiar").assertExists()
        rule.onNodeWithText("Seguidores").assertExists()
        rule.onNodeWithText("Sobre mí").assertExists()
    }

    @Test fun imagenAvatarYEtiquetaRenderizanEnPreview() {
        rule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ImagenEncabezadoPreview()
                AvatarCircularPreview()
                EtiquetaInteresPreview()
            }
        }
        rule.onNodeWithText("Únete a la comunidad").assertExists()
        rule.onNodeWithText("Completa tus datos en menos de un minuto").assertExists()
        rule.onNodeWithText("Kotlin").assertExists()
    }
}
