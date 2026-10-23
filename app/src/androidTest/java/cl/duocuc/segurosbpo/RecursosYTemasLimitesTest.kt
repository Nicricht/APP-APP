package cl.duocuc.segurosbpo

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecursosYTemasLimitesTest {
    @get:Rule val rule = createComposeRule()

    @Test fun proveedorSinTipoMimeRechazaElDocumentoConMensajeClaro() {
        val uri = Uri.Builder()
            .scheme("content")
            .authority("cl.duocuc.segurosbpo.qa.archivos")
            .appendPath("archivo")
            .appendQueryParameter("contenido", "")
            .build()
        val context = InstrumentationRegistry.getInstrumentation().context
        val resultado = runCatching { ProcesadorEvidencia.desdeSelector(context, uri) }
        assertTrue(resultado.isFailure)
        assertEquals(
            "Selecciona una imagen JPG, PNG, WEBP o un PDF",
            resultado.exceptionOrNull()?.message
        )
    }

    @Test fun temaPredeterminadoRespetaConfiguracionDelDispositivo() {
        rule.setContent {
            SegurosBpoTheme {
                Text("Interfaz predeterminada del sistema")
            }
        }
        rule.onNodeWithText("Interfaz predeterminada del sistema").assertExists()
    }

    @Test fun temaConOpcionesExplicitasAdmiteContenidoAnidado() {
        rule.setContent {
            SegurosBpoTheme(darkTheme = true, dynamicColor = false) {
                SegurosBpoTheme(darkTheme = false, dynamicColor = false) {
                    Text("Contraste y jerarquía visual")
                }
            }
        }
        rule.onNodeWithText("Contraste y jerarquía visual").assertExists()
    }
}
