package cl.duocuc.segurosbpo

import android.view.ContextThemeWrapper
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme
import cl.duocuc.segurosbpo.viewmodel.FormularioViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParametrosPredeterminadosComposeTest {
    @get:Rule val rule = createComposeRule()

    @Test fun formularioConModeloPredeterminadoActualizaSuEstadoAlEditar() {
        val propietario = object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
        val modelo = ViewModelProvider(propietario)[FormularioViewModel::class.java]
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides propietario) {
                FormularioScreen(
                    aseguradoActual = Asegurado.CAMILA,
                    onRegistroExitoso = { "SIN-26-008080" },
                    onVolver = {},
                    onVerMisSiniestros = {}
                )
            }
        }
        rule.onNodeWithText("Volver a mis siniestros").assertExists()
        rule.runOnIdle {
            modelo.onFechaCambia("20-10-2026")
            modelo.onComunaCambia("Santiago")
            modelo.onDescripcionCambia("Pérdida informada en prueba")
        }
        rule.waitForIdle()
        assertEquals("Santiago", modelo.uiState.value.comuna)
        rule.runOnIdle { modelo.onDatosCorrectosCambia(true) }
        rule.waitForIdle()
        assertEquals(true, modelo.uiState.value.datosCorrectos)
        rule.onNodeWithText("Volver a mis siniestros").assertExists()
        propietario.viewModelStore.clear()
    }

    @Test fun temaPredeterminadoSigueActivoAlCambiarContextoAndroid() {
        val cambiar = mutableStateOf(false)
        rule.setContent {
            val original = LocalContext.current
            val alternativo = remember(original) {
                ContextThemeWrapper(original, android.R.style.Theme_Material_Light)
            }
            CompositionLocalProvider(
                LocalContext provides if (cambiar.value) alternativo else original
            ) {
                SegurosBpoTheme {
                    Text("Tema dinámico con contexto válido")
                }
            }
        }
        rule.onNodeWithText("Tema dinámico con contexto válido").assertExists()
        rule.runOnIdle { cambiar.value = true }
        rule.onNodeWithText("Tema dinámico con contexto válido").assertExists()
        rule.runOnIdle { cambiar.value = false }
        rule.onNodeWithText("Tema dinámico con contexto válido").assertExists()
    }
}
