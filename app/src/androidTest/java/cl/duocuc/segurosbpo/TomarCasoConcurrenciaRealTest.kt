package cl.duocuc.segurosbpo

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TomarCasoConcurrenciaRealTest {
    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val nombre = "qa_tomar_caso_concurrente.db"
    private lateinit var base: SiniestroDbHelper

    @Before fun preparar() {
        contexto.deleteDatabase(nombre)
        base = SiniestroDbHelper(contexto, nombre)
    }

    @After fun limpiar() {
        base.close()
        contexto.deleteDatabase(nombre)
    }

    @Test fun dosHilosSimultaneosSoloGeneranUnaAsignacionYUnAviso() {
        val siniestro = base.listar(Asegurado.CAMILA).single()
        val gestionesAntes = base.listarGestiones(siniestro.id, Asegurado.CAMILA).size
        val avisosAntes = base.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == siniestro.id }
        val primero = SiniestroDbHelper(contexto, nombre)
        val segundo = SiniestroDbHelper(contexto, nombre)
        val ejecutor = Executors.newFixedThreadPool(2)
        val preparados = CountDownLatch(2)
        val inicio = CountDownLatch(1)
        try {
            primero.readableDatabase
            segundo.readableDatabase
            val intentos = listOf(primero, segundo).map { conexion ->
                ejecutor.submit(Callable {
                    preparados.countDown()
                    check(inicio.await(15, TimeUnit.SECONDS))
                    runCatching { conexion.tomarCaso(siniestro.id, Liquidador.VEHICULOS) }
                        .getOrDefault(false)
                })
            }
            assertTrue(preparados.await(15, TimeUnit.SECONDS))
            inicio.countDown()
            val resultados = intentos.map { it.get(30, TimeUnit.SECONDS) }
            assertEquals(1, resultados.count { it })
            assertEquals(1, resultados.count { !it })
            assertEquals(EtapaSiniestro.EVALUACION, base.obtener(siniestro.id, Asegurado.CAMILA)?.etapa)
            assertEquals(Liquidador.VEHICULOS, base.obtener(siniestro.id, Asegurado.CAMILA)?.liquidadorAsignado)
            assertFalse(base.listarDisponibles(Liquidador.VEHICULOS).any { it.id == siniestro.id })
            assertEquals(gestionesAntes + 1, base.listarGestiones(siniestro.id, Asegurado.CAMILA).size)
            assertEquals(avisosAntes + 1, base.listarAvisos(Asegurado.CAMILA).count { it.siniestroId == siniestro.id })
        } finally {
            inicio.countDown()
            ejecutor.shutdownNow()
            assertTrue(ejecutor.awaitTermination(10, TimeUnit.SECONDS))
            primero.close()
            segundo.close()
        }
    }
}
