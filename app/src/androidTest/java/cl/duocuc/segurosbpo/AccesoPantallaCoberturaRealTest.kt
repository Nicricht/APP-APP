package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.ui.screens.AccesoScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class AccesoPantallaCoberturaRealTest {
    @get:Rule val rule = createComposeRule()

    @Test fun loginEnviaCredencialesYPresentaErrorReal() {
        val recibidas = AtomicReference<Pair<String, String>>()
        rule.setContent {
            AccesoScreen(
                onIniciar = { correo, clave ->
                    recibidas.set(correo to clave)
                    "Correo o contraseña incorrectos"
                },
                onRegistrar = { _, _, _, _, _, _ ->
                    throw AssertionError("Registro no solicitado")
                },
                onDemoLiquidador = {},
                onDemoAsegurado = {}
            )
        }
        rule.onNodeWithText("Correo electrónico").performTextInput("noexiste@ejemplo.cl")
        rule.onNodeWithText("Contraseña de demostración").performTextInput("ClaveIncorrecta")
        rule.onAllNodesWithText("Iniciar sesión").onLast().performClick()
        assertEquals("noexiste@ejemplo.cl" to "ClaveIncorrecta", recibidas.get())
        rule.onNodeWithText("Correo o contraseña incorrectos").assertExists()
    }

    @Test fun registroCompletoEnviaCamposYPermiteRegresarAlLogin() {
        val datos = AtomicReference<List<String>>()
        rule.setContent {
            AccesoScreen(
                onIniciar = { _, _ -> null },
                onRegistrar = { a, b, c, d, e, f ->
                    datos.set(listOf(a, b, c, d, e, f))
                    "RUT ficticio inválido"
                },
                onDemoLiquidador = {}, onDemoAsegurado = {}
            )
        }
        rule.onNodeWithText("Crear cuenta de asegurado").performScrollTo().performClick()
        rule.onNodeWithText("Nombre completo").performScrollTo().performTextInput("Ana Prueba")
        rule.onNodeWithText("RUT ficticio").performScrollTo().performTextInput("12.345.678-5")
        rule.onNodeWithText("Correo electrónico").performScrollTo().performTextInput("ana@ejemplo.cl")
        rule.onNodeWithText("Teléfono").performScrollTo().performTextInput("912345678")
        rule.onNodeWithText("Contraseña de demostración").performScrollTo().performTextInput("Clave2026")
        rule.onNodeWithText("Confirmar contraseña").performScrollTo().performTextInput("Clave2026")
        rule.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        assertEquals(
            listOf("Ana Prueba", "12.345.678-5", "ana@ejemplo.cl", "912345678", "Clave2026", "Clave2026"),
            datos.get()
        )
        rule.onNodeWithText("RUT ficticio inválido").assertExists()
        rule.onNodeWithText("Ya tengo cuenta").performScrollTo().performClick()
        rule.onNodeWithText("RUT ficticio").assertDoesNotExist()
        rule.onNodeWithText("RUT ficticio inválido").assertDoesNotExist()
    }

    @Test fun registroInvalidoMuestraErroresPorCampoYActualizaSoloElCorregido() {
        rule.setContent {
            AccesoScreen(
                onIniciar = { _, _ -> null },
                onRegistrar = { _, _, _, _, _, _ -> "Revisa los datos" },
                onDemoLiquidador = {}, onDemoAsegurado = {}
            )
        }
        rule.onNodeWithText("Crear cuenta de asegurado").performScrollTo().performClick()
        rule.onNodeWithText("Crear cuenta").performScrollTo().performClick()

        rule.onNodeWithText("Ingresa tu nombre completo").assertExists()
        rule.onNodeWithText("Ingresa un RUT ficticio válido").assertExists()
        rule.onNodeWithText("Ingresa un correo válido").assertExists()
        rule.onNodeWithText("Revisa los datos").assertExists()

        rule.onNodeWithText("Nombre completo").performScrollTo().performTextInput("Ana Prueba")
        rule.onNodeWithText("Ingresa tu nombre completo").assertDoesNotExist()
        rule.onNodeWithText("Ingresa un RUT ficticio válido").assertExists()
        rule.onNodeWithText("Revisa los datos").assertDoesNotExist()
    }

    @Test fun accesosDemoDistinguenVehiculosYHogar() {
        val elegido = AtomicReference<Liquidador>()
        val camila = AtomicBoolean(false)
        rule.setContent {
            AccesoScreen(
                onIniciar = { _, _ -> null }, onRegistrar = { _, _, _, _, _, _ -> null },
                onDemoLiquidador = { elegido.set(it) },
                onDemoAsegurado = { camila.set(true) }
            )
        }
        rule.onNodeWithText("Liquidador de Hogar (demo)").performScrollTo().performClick()
        assertEquals(Liquidador.HOGAR, elegido.get())
        rule.onNodeWithText("Liquidador de Vehículos (demo)").performScrollTo().performClick()
        assertEquals(Liquidador.VEHICULOS, elegido.get())
        rule.onNodeWithText("Continuar como Camila (demo)").performScrollTo().performClick()
        assertTrue(camila.get())
    }
}
