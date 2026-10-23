package cl.duocuc.segurosbpo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.ui.components.CampoContrasena
import cl.duocuc.segurosbpo.ui.components.CampoTexto
import cl.duocuc.segurosbpo.ui.components.CasillaConTexto
import cl.duocuc.segurosbpo.ui.components.GrupoRadio
import cl.duocuc.segurosbpo.ui.components.ImagenEncabezado
import cl.duocuc.segurosbpo.ui.components.InterruptorConTexto
import cl.duocuc.segurosbpo.ui.components.SelectorOpciones
import cl.duocuc.segurosbpo.ui.components.TarjetaEstadistica
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModificadoresComponentesCoberturaTest {
    @get:Rule val rule = createComposeRule()

    @Test fun controlesConEspaciadoPersonalizadoRespondenALasAcciones() {
        rule.setContent {
            var aceptado by remember { mutableStateOf(false) }
            var avisos by remember { mutableStateOf(false) }
            var opcion by remember { mutableStateOf("Uno") }
            Column {
                CasillaConTexto("Aceptación", aceptado, { aceptado = it }, Modifier.padding(4.dp))
                InterruptorConTexto("Notificaciones", avisos, { avisos = it }, Modifier.padding(4.dp))
                GrupoRadio(
                    opciones = listOf("Uno", "Dos"),
                    seleccionada = opcion,
                    onSeleccionar = { opcion = it },
                    textoDe = { it },
                    modifier = Modifier.padding(4.dp)
                )
                Text("Aceptado: " + aceptado)
                Text("Avisos: " + avisos)
                Text("Elegido: " + opcion)
            }
        }
        rule.onNodeWithText("Aceptación").performClick()
        rule.onNodeWithText("Notificaciones").performClick()
        rule.onNodeWithText("Dos").performClick()
        rule.onNodeWithText("Aceptado: true").assertExists()
        rule.onNodeWithText("Avisos: true").assertExists()
        rule.onNodeWithText("Elegido: Dos").assertExists()
    }

    @Test fun selectorConEspaciadoPersonalizadoMantieneSuSeleccion() {
        rule.setContent {
            var opcion by remember { mutableStateOf("Uno") }
            SelectorOpciones(
                titulo = "Selector de prueba",
                opciones = listOf("Uno", "Dos"),
                seleccionada = opcion,
                onSeleccionar = { opcion = it },
                textoDe = { it },
                modifier = Modifier.padding(8.dp)
            )
        }
        rule.onNodeWithText("Cambiar").performClick()
        rule.onNodeWithText("Dos").performClick()
        rule.onNodeWithText("Dos").assertExists()
    }

    @Test fun tarjetasConYSinModificadorExplicitoMuestranSusDatos() {
        rule.setContent {
            Column {
                TarjetaEstadistica("8", "Pendientes")
                TarjetaEstadistica("3", "Cerrados", Modifier.padding(8.dp))
            }
        }
        rule.onNodeWithText("8").assertExists()
        rule.onNodeWithText("Pendientes").assertExists()
        rule.onNodeWithText("3").assertExists()
        rule.onNodeWithText("Cerrados").assertExists()
    }

    @Test fun imagenConModificadorExplicitoConservaSuDescripcion() {
        rule.setContent {
            ImagenEncabezado(
                imagen = R.drawable.banner_registro,
                descripcion = "Imagen de póliza",
                titulo = "Seguro activo",
                subtitulo = "Información protegida",
                modifier = Modifier.padding(8.dp)
            )
        }
        rule.onNodeWithContentDescription("Imagen de póliza").assertExists()
        rule.onNodeWithText("Seguro activo").assertExists()
        rule.onNodeWithText("Información protegida").assertExists()
    }

    @Test fun claveConEspaciadoAceptaTextoYMuestraSuEstado() {
        rule.setContent {
            var clave by remember { mutableStateOf("") }
            var visible by remember { mutableStateOf(false) }
            Column {
                CampoContrasena(
                    valor = clave,
                    onValorCambia = { clave = it },
                    etiqueta = "Clave de prueba",
                    visible = visible,
                    onAlternarVisible = { visible = !visible },
                    modifier = Modifier.padding(8.dp)
                )
                Text("Longitud: " + clave.length)
            }
        }
        rule.onNodeWithText("Clave de prueba").performTextInput("2026")
        rule.onNodeWithText("Longitud: 4").assertExists()
        rule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        rule.onNodeWithContentDescription("Ocultar Contraseña").assertExists()
    }

    @Test fun campoMultilineaConModificadorConservaElValor() {
        rule.setContent {
            var texto by remember { mutableStateOf("") }
            CampoTexto(
                valor = texto,
                onValorCambia = { texto = it },
                etiqueta = "Observaciones",
                icono = R.drawable.ic_correo,
                modifier = Modifier.padding(8.dp),
                multilinea = true
            )
        }
        rule.onNodeWithText("Observaciones").performTextInput("Documento revisado")
        rule.onNodeWithText("Documento revisado").assertExists()
    }
}
