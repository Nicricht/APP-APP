package cl.duocuc.segurosbpo

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duocuc.segurosbpo.navigation.idDesdeArgumentos
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
}
