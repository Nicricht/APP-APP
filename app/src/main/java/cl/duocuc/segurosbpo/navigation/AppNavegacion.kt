package cl.duocuc.segurosbpo.navigation

import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.segurosbpo.ui.screens.AvisosScreen
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import cl.duocuc.segurosbpo.ui.screens.HistorialSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EditarSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel

fun idDesdeArgumentos(argumentos: Bundle?): Long = argumentos?.getLong("id") ?: 0L

fun contarAvisosNoLeidos(avisos: List<cl.duocuc.segurosbpo.model.AvisoSiniestro>): Int =
    avisos.count { !it.leido }

fun actualizarDescripcionYRegresar(
    actualizar: (Long, String) -> Boolean,
    id: Long,
    descripcion: String,
    regresar: () -> Unit
) {
    if (actualizar(id, descripcion)) regresar()
}

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val siniestrosVm: SiniestrosViewModel = viewModel()
    val asegurado by siniestrosVm.asegurado.collectAsStateWithLifecycle()
    val siniestros by siniestrosVm.siniestros.collectAsStateWithLifecycle()
    val busqueda by siniestrosVm.busqueda.collectAsStateWithLifecycle()
    val evidencias by siniestrosVm.evidencias.collectAsStateWithLifecycle()
    val solicitudes by siniestrosVm.solicitudes.collectAsStateWithLifecycle()
    val solicitudesLiquidador by siniestrosVm.solicitudesLiquidador.collectAsStateWithLifecycle()
    val avisos by siniestrosVm.avisos.collectAsStateWithLifecycle()
    val gestiones by siniestrosVm.gestiones.collectAsStateWithLifecycle()
    val liquidador by siniestrosVm.liquidador.collectAsStateWithLifecycle()
    val casosLiquidador by siniestrosVm.casosLiquidador.collectAsStateWithLifecycle()
    val gestionesLiquidador by siniestrosVm.gestionesLiquidador.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Ruta.MisSiniestros.ruta) {
        composable(Ruta.MisSiniestros.ruta) {
            MisSiniestrosScreen(
                asegurado = asegurado,
                siniestros = siniestros,
                busqueda = busqueda,
                onBuscar = siniestrosVm::buscar,
                avisosNoLeidos = contarAvisosNoLeidos(avisos),
                onVerAvisos = { navController.navigate(Ruta.Avisos.ruta) },
                onCambiarAsegurado = siniestrosVm::seleccionarAsegurado,
                onReportar = { navController.navigate(Ruta.Reportar.ruta) },
                onGestionar = { navController.navigate(Ruta.Liquidador.ruta) },
                onVerDetalle = { navController.navigate(Ruta.Detalle.crear(it)) }
            )
        }
        composable(Ruta.Avisos.ruta) {
            AvisosScreen(
                avisos = avisos,
                onCargar = siniestrosVm::refrescarAvisos,
                onMarcarLeido = siniestrosVm::marcarAvisoLeido,
                onAbrirCaso = { navController.navigate(Ruta.Detalle.crear(it)) },
                onVolver = { navController.navigateUp() }
            )
        }
        composable(Ruta.Liquidador.ruta) {
            CasosLiquidadorScreen(
                liquidador = liquidador,
                casos = casosLiquidador,
                gestiones = gestionesLiquidador,
                solicitudes = solicitudesLiquidador,
                onSolicitarDocumento = siniestrosVm::solicitarDocumentoLiquidador,
                onSeleccionarLiquidador = siniestrosVm::seleccionarLiquidador,
                onCargarGestiones = siniestrosVm::cargarGestionesLiquidador,
                onRegistrarGestion = siniestrosVm::registrarGestionLiquidador,
                onAvanzar = siniestrosVm::avanzarLiquidador,
                onCerrarAnticipado = siniestrosVm::cerrarAnticipadoLiquidador,
                onVolver = { navController.navigateUp() }
            )
        }
        composable(Ruta.Reportar.ruta) {
            FormularioScreen(
                aseguradoActual = asegurado,
                onRegistroExitoso = siniestrosVm::registrar,
                onVolver = { navController.navigateUp() },
                onVerMisSiniestros = {
                    navController.popBackStack(Ruta.MisSiniestros.ruta, false)
                }
            )
        }
        composable(
            route = Ruta.Detalle.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = idDesdeArgumentos(entrada.arguments)
            DetalleSiniestroScreen(
                siniestro = siniestrosVm.obtener(id),
                solicitudes = solicitudes,
                onCargarSolicitudes = { siniestrosVm.refrescarEvidencias(id) },
                onVolver = { navController.navigateUp() },
                onEditar = { navController.navigate(Ruta.Editar.crear(id)) },
                onEvidencias = { navController.navigate(Ruta.Evidencias.crear(id)) },
                onHistorial = { navController.navigate(Ruta.Historial.crear(id)) }
            )
        }
        composable(
            route = Ruta.Historial.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = idDesdeArgumentos(entrada.arguments)
            HistorialSiniestroScreen(
                siniestro = siniestrosVm.obtener(id),
                gestiones = gestiones,
                onCargar = { siniestrosVm.refrescarGestiones(id) },
                onVolver = { navController.navigateUp() }
            )
        }
        composable(
            route = Ruta.Evidencias.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = idDesdeArgumentos(entrada.arguments)
            EvidenciasScreen(
                siniestro = siniestrosVm.obtener(id),
                asegurado = asegurado,
                evidencias = evidencias,
                solicitudes = solicitudes,
                onVolver = { navController.navigateUp() },
                onCargar = { siniestrosVm.refrescarEvidencias(id) },
                onCargarImagen = { evidenciaId -> siniestrosVm.cargarImagen(id, evidenciaId) },
                onGuardarFoto = { tipo, bitmap -> siniestrosVm.guardarFoto(id, tipo, bitmap) },
                onGuardarArchivo = { tipo, uri -> siniestrosVm.guardarArchivo(id, tipo, uri) }
            )
        }
        composable(
            route = Ruta.Editar.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = idDesdeArgumentos(entrada.arguments)
            EditarSiniestroScreen(
                siniestro = siniestrosVm.obtener(id),
                onVolver = { navController.navigateUp() },
                onGuardar = { descripcion ->
                    actualizarDescripcionYRegresar(
                        siniestrosVm::actualizarDescripcion,
                        id,
                        descripcion
                    ) {
                        navController.popBackStack(Ruta.MisSiniestros.ruta, false)
                    }
                }
            )
        }
    }
}
