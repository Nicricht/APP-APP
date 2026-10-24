package cl.duocuc.segurosbpo

import android.graphics.Bitmap
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.ui.screens.PerfilCuentaScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class PerfilCuentaCoberturaRealTest {
    @get:Rule val rule = createComposeRule()

    private fun cuenta(
        rol: RolCuenta = RolCuenta.ASEGURADO,
        correo: String = "ana@ejemplo.cl",
        foto: ByteArray? = null
    ) = CuentaLocal(
        id = "USR_PRUEBA", nombre = "Ana Prueba", rut = "12.345.678-5",
        correo = correo, telefono = "912345678", rol = rol,
        equipo = if (rol == RolCuenta.LIQUIDADOR) "HOGAR" else null,
        foto = foto
    )

    @Test fun aseguradoActualizaTelefonoYVuelveSinCerrarSesion() {
        val guardado = AtomicReference<String>()
        val regreso = AtomicBoolean(false)
        val salir = AtomicBoolean(false)
        rule.setContent {
            PerfilCuentaScreen(
                cuenta = cuenta(),
                onGuardarTelefono = { guardado.set(it); null },
                onGuardarFoto = { null },
                onVolver = { regreso.set(true) },
                onCerrarSesion = { salir.set(true) }
            )
        }
        rule.onNodeWithText("Ana Prueba").assertExists()
        rule.onNodeWithText("Correo: ana@ejemplo.cl").assertExists()
        rule.onNodeWithText("Teléfono de contacto").performScrollTo().performTextClearance()
        rule.onNodeWithText("Teléfono de contacto").performTextInput("987654321")
        rule.onNodeWithText("Guardar teléfono").performScrollTo().performClick()
        rule.onNodeWithText("Datos actualizados").assertExists()
        assertEquals("987654321", guardado.get())
        rule.onNodeWithText("Volver").performScrollTo().performClick()
        assertTrue(regreso.get())
        assertTrue(!salir.get())
    }

    @Test fun errorAlGuardarTelefonoNoSeConfundeConExito() {
        rule.setContent {
            PerfilCuentaScreen(
                cuenta = cuenta(), onGuardarTelefono = { "Teléfono inválido" },
                onGuardarFoto = { null }, onVolver = {},
                onCerrarSesion = {}
            )
        }
        rule.onNodeWithText("Guardar teléfono").performScrollTo().performClick()
        rule.onNodeWithText("Teléfono inválido").assertExists()
    }

    @Test fun liquidadorDemoMuestraEquipoYBloqueaEdicion() {
        val salir = AtomicBoolean(false)
        rule.setContent {
            PerfilCuentaScreen(
                cuenta = cuenta(RolCuenta.LIQUIDADOR, ""),
                onGuardarTelefono = { throw AssertionError("No debe editar") },
                onGuardarFoto = { throw AssertionError("No debe editar") },
                onVolver = {},
                onCerrarSesion = { salir.set(true) }
            )
        }
        rule.onNodeWithText("Liquidador").assertExists()
        rule.onNodeWithText("Equipo: HOGAR").assertExists()
        rule.onNodeWithText("Perfil ficticio de demostración. Los datos no son editables.")
            .assertExists()
        rule.onNodeWithText("Guardar teléfono").assertDoesNotExist()
        rule.onNodeWithText("Tomar foto de perfil").assertDoesNotExist()
        rule.onNodeWithText("Cerrar sesión").performScrollTo().performClick()
        assertTrue(salir.get())
    }

    @Test fun muestraFotoGuardadaSinSolicitarPermisosNuevos() {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, salida)
        bitmap.recycle()
        rule.setContent {
            PerfilCuentaScreen(
                cuenta = cuenta(foto = salida.toByteArray()),
                onGuardarTelefono = { null }, onGuardarFoto = { null },
                onVolver = {}, onCerrarSesion = {}
            )
        }
        rule.onNodeWithContentDescription("Foto de perfil").assertIsDisplayed()
    }
}
