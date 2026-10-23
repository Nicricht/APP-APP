package cl.duocuc.segurosbpo

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.ArchivoEvidencia
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImagenesSQLiteLimitesTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_imagenes_grandes.db"
    private lateinit var db: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
        db = SiniestroDbHelper(contexto, nombre)
    }

    @After fun limpiar() {
        db.close()
        contexto.deleteDatabase(nombre)
    }

    @Test fun fotografiaGrandeSeReduceAlConsultarYNoSeEntregaAOtroAsegurado() {
        val caso = db.registrar(FormularioUiState(
            asegurado = Asegurado.CAMILA,
            fechaOcurrencia = "20-10-2026",
            comuna = "Quilicura",
            descripcion = "Prueba de resolución de imagen"
        ))
        val foto = Bitmap.createBitmap(1500, 1200, Bitmap.Config.ARGB_8888)
        try {
            db.guardarEvidencia(caso.id, Asegurado.CAMILA, "Fotografía grande", ProcesadorEvidencia.desdeCamara(foto))
        } finally {
            foto.recycle()
        }
        val evidencia = db.listarEvidencias(caso.id, Asegurado.CAMILA).single()
        val imagen = db.obtenerImagen(evidencia.id, caso.id, Asegurado.CAMILA)
        assertNotNull(imagen)
        try {
            assertTrue(imagen!!.width <= 900)
            assertTrue(imagen.height <= 900)
        } finally {
            imagen?.recycle()
        }
        assertNull(db.obtenerImagen(evidencia.id, caso.id, Asegurado.JORGE))
        assertNull(db.obtenerImagen(evidencia.id, -1, Asegurado.CAMILA))
    }

    @Test fun datosNoDecodificablesNoProvocanErrorAlConsultar() {
        val caso = db.registrar(FormularioUiState(
            asegurado = Asegurado.CAMILA,
            fechaOcurrencia = "20-10-2026",
            comuna = "Santiago",
            descripcion = "Archivo inválido guardado localmente"
        ))
        db.guardarEvidencia(
            caso.id,
            Asegurado.CAMILA,
            "Fotografía corrupta",
            ArchivoEvidencia("foto.jpg", "image/jpeg", byteArrayOf(1, 2, 3, 4, 5))
        )
        val evidencia = db.listarEvidencias(caso.id, Asegurado.CAMILA).single()
        assertNull(db.obtenerImagen(evidencia.id, caso.id, Asegurado.CAMILA))
    }

    @Test
    fun fotografiaVerticalSeReduceAunqueElAnchoSeaMenorAlLimite() {
        val caso = db.registrar(FormularioUiState(
            asegurado = Asegurado.CAMILA,
            fechaOcurrencia = "20-10-2026",
            comuna = "Santiago",
            descripcion = "Comprobación de una fotografía vertical"
        ))
        val foto = Bitmap.createBitmap(480, 1800, Bitmap.Config.ARGB_8888)
        try {
            db.guardarEvidencia(
                caso.id,
                Asegurado.CAMILA,
                "Fotografía vertical",
                ProcesadorEvidencia.desdeCamara(foto)
            )
        } finally {
            foto.recycle()
        }
        val evidencia = db.listarEvidencias(caso.id, Asegurado.CAMILA).single()
        val resultado = db.obtenerImagen(evidencia.id, caso.id, Asegurado.CAMILA)
        assertNotNull(resultado)
        try {
            assertTrue(resultado!!.width <= 900)
            assertTrue(resultado.height <= 900)
        } finally {
            resultado?.recycle()
        }
    }
}
