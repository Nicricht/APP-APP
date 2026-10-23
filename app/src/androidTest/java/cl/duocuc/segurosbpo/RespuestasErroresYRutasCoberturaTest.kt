package cl.duocuc.segurosbpo

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.navigation.idDesdeArgumentos
import cl.duocuc.segurosbpo.navigation.actualizarDescripcionYRegresar
import cl.duocuc.segurosbpo.navigation.contarAvisosNoLeidos
import cl.duocuc.segurosbpo.model.AvisoSiniestro
import cl.duocuc.segurosbpo.viewmodel.mensajeError
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RespuestasErroresYRutasCoberturaTest {

    @Test fun errorSinMensajeEntregaUnaExplicacionDeRespaldo() {
        assertEquals(
            "No se pudo guardar el documento",
            mensajeError(IllegalStateException(), "No se pudo guardar el documento")
        )
        assertEquals(
            "El documento no pertenece al asegurado",
            mensajeError(
                IllegalArgumentException("El documento no pertenece al asegurado"),
                "No se pudo guardar el documento"
            )
        )
    }

    @Test fun argumentosDeRutaRecuperanElIdentificadorYSonSegurosSiFaltan() {
        val recibidos = Bundle().apply { putLong("id", 57L) }
        assertEquals(57L, idDesdeArgumentos(recibidos))
        assertEquals(0L, idDesdeArgumentos(Bundle()))
        assertEquals(0L, idDesdeArgumentos(null))
    }

    @Test fun navegacionSoloRegresaCuandoSeActualizaLaDescripcion() {
        var regresos = 0
        var recibida = ""
        val guardar: (Long, String) -> Boolean = { id, descripcion ->
            recibida = "$id:$descripcion"
            id == 15L
        }
        actualizarDescripcionYRegresar(guardar, 999L, "Caso inexistente") {
            regresos++
        }
        assertEquals("999:Caso inexistente", recibida)
        assertEquals(0, regresos)
        actualizarDescripcionYRegresar(guardar, 15L, "Caso editable") {
            regresos++
        }
        assertEquals("15:Caso editable", recibida)
        assertEquals(1, regresos)
    }


    @Test fun contarAvisosDistingueLeidosYPendientes() {
        val primero = AvisoSiniestro(1, 7, "SIN-26-7", "Documento solicitado", "20-10-2026", false)
        val segundo = primero.copy(id = 2, leido = true)
        val tercero = primero.copy(id = 3, leido = false)
        assertEquals(0, contarAvisosNoLeidos(emptyList()))
        assertEquals(1, contarAvisosNoLeidos(listOf(primero, segundo)))
        assertEquals(2, contarAvisosNoLeidos(listOf(primero, segundo, tercero)))
        assertEquals(0, contarAvisosNoLeidos(listOf(segundo)))
    }
}
