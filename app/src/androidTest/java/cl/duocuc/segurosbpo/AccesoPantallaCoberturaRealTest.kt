package cl.duocuc.segurosbpo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
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
import java.util.concurrent.atomic.AtomicInteger
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

    @Test fun losSeisErroresDeRegistroSonVisiblesYDesaparecenAlCorregirCadaCampo() {
        val enviados = AtomicReference<List<String>>()
        val intentos = AtomicInteger(0)
        rule.setContent {
            AccesoScreen(
                onIniciar = { _, _ -> throw AssertionError("No debe iniciar sesión") },
                onRegistrar = { nombre, rut, correo, telefono, clave, confirmacion ->
                    intentos.incrementAndGet()
                    enviados.set(listOf(nombre, rut, correo, telefono, clave, confirmacion))
                    if (nombre == "Ana Prueba" && rut == "26.354.719-5" &&
                        correo == "ana@ejemplo.cl" && telefono == "912345678" &&
                        clave == "Clave2026" && confirmacion == "Clave2026"
                    ) null else "Corrige los datos del formulario"
                },
                onDemoLiquidador = {}, onDemoAsegurado = {}
            )
        }
        rule.onNodeWithText("Crear cuenta de asegurado").performScrollTo().performClick()
        rule.onNodeWithText("Contraseña de demostración").performScrollTo()
            .performTextInput("corta")
        rule.onNodeWithText("Confirmar contraseña").performScrollTo()
            .performTextInput("distinta")
        rule.onNodeWithText("Crear cuenta").performScrollTo().performClick()

        val errores = listOf(
            "Ingresa tu nombre completo",
            "Ingresa un RUT ficticio válido",
            "Ingresa un correo válido",
            "Ingresa un teléfono válido",
            "Usa una clave de demostración de al menos 8 caracteres",
            "Las claves no coinciden"
        )
        errores.forEach { rule.onNodeWithText(it).assertExists() }
        rule.onNodeWithText("Corrige los datos del formulario").assertExists()
        assertEquals(1, intentos.get())

        rule.onNodeWithText("Nombre completo").performScrollTo().performTextInput("Ana Prueba")
        rule.onNodeWithText(errores[0]).assertDoesNotExist()
        rule.onNodeWithText(errores[1]).assertExists()

        rule.onNodeWithText("RUT ficticio").performScrollTo().performTextInput("26.354.719-5")
        rule.onNodeWithText(errores[1]).assertDoesNotExist()
        rule.onNodeWithText(errores[2]).assertExists()

        rule.onNodeWithText("Correo electrónico").performScrollTo()
            .performTextInput("ana@ejemplo.cl")
        rule.onNodeWithText(errores[2]).assertDoesNotExist()
        rule.onNodeWithText(errores[3]).assertExists()

        rule.onNodeWithText("Teléfono").performScrollTo().performTextInput("912345678")
        rule.onNodeWithText(errores[3]).assertDoesNotExist()
        rule.onNodeWithText(errores[4]).assertExists()

        rule.onNodeWithText("Contraseña de demostración").performScrollTo()
            .performTextClearance()
        rule.onNodeWithText("Contraseña de demostración").performScrollTo()
            .performTextInput("Clave2026")
        rule.onNodeWithText(errores[4]).assertDoesNotExist()
        rule.onNodeWithText(errores[5]).assertExists()

        rule.onNodeWithText("Confirmar contraseña").performScrollTo()
            .performTextClearance()
        rule.onNodeWithText("Confirmar contraseña").performScrollTo()
            .performTextInput("Clave2026")
        errores.forEach { rule.onNodeWithText(it).assertDoesNotExist() }
        rule.onNodeWithText("Corrige los datos del formulario").assertDoesNotExist()

        rule.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        assertEquals(2, intentos.get())
        assertEquals(
            listOf("Ana Prueba", "26.354.719-5", "ana@ejemplo.cl", "912345678",
                "Clave2026", "Clave2026"),
            enviados.get()
        )
        errores.forEach { rule.onNodeWithText(it).assertDoesNotExist() }
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
