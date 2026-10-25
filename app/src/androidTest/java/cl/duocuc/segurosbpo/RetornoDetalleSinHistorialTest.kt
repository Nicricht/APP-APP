package cl.duocuc.segurosbpo

import androidx.navigation.NavHostController
import androidx.navigation.createGraph
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.navigation.Ruta
import cl.duocuc.segurosbpo.navigation.volverDelDetalle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RetornoDetalleSinHistorialTest {
    private val rutaDetalle = "detalle_de_prueba"

    private fun controlador(inicio: String): NavHostController {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        return NavHostController(contexto).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createGraph(startDestination = inicio) {
                composable(Ruta.MisSiniestros.ruta) { }
                composable(rutaDetalle) { }
            }
        }
    }

    @Test fun detalleAbiertoSinPantallaAnteriorRegresaAlListadoSeguro() {
        val controlador = controlador(rutaDetalle)

        volverDelDetalle(controlador)

        assertEquals(Ruta.MisSiniestros.ruta, controlador.currentDestination?.route)
    }

    @Test fun detalleConHistorialRetrocedeSinDuplicarElListado() {
        val controlador = controlador(Ruta.MisSiniestros.ruta)
        controlador.navigate(rutaDetalle)
        assertEquals(rutaDetalle, controlador.currentDestination?.route)

        volverDelDetalle(controlador)

        assertEquals(Ruta.MisSiniestros.ruta, controlador.currentDestination?.route)
        assertNull(controlador.previousBackStackEntry)
    }
}
