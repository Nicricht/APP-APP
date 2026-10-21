package cl.duocuc.segurosbpo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import cl.duocuc.segurosbpo.ui.screens.EditarSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import cl.duocuc.segurosbpo.viewmodel.SiniestrosViewModel

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val siniestrosVm: SiniestrosViewModel = viewModel()
    val asegurado by siniestrosVm.asegurado.collectAsStateWithLifecycle()
    val siniestros by siniestrosVm.siniestros.collectAsStateWithLifecycle()
    val busqueda by siniestrosVm.busqueda.collectAsStateWithLifecycle()
    val evidencias by siniestrosVm.evidencias.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Ruta.MisSiniestros.ruta) {
        composable(Ruta.MisSiniestros.ruta) {
            MisSiniestrosScreen(
                asegurado = asegurado,
                siniestros = siniestros,
                busqueda = busqueda,
                onBuscar = siniestrosVm::buscar,
                onCambiarAsegurado = siniestrosVm::seleccionarAsegurado,
                onReportar = { navController.navigate(Ruta.Reportar.ruta) },
                onVerDetalle = { navController.navigate(Ruta.Detalle.crear(it)) }
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
            val id = entrada.arguments?.getLong("id") ?: 0L
            DetalleSiniestroScreen(
                siniestro = siniestrosVm.obtener(id),
                onVolver = { navController.navigateUp() },
                onEditar = { navController.navigate(Ruta.Editar.crear(id)) },
                onEvidencias = { navController.navigate(Ruta.Evidencias.crear(id)) }
            )
        }
        composable(
            route = Ruta.Evidencias.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            EvidenciasScreen(
                siniestro = siniestrosVm.obtener(id),
                asegurado = asegurado,
                evidencias = evidencias,
                onVolver = { navController.navigateUp() },
                onCargar = { siniestrosVm.refrescarEvidencias(id) },
                onGuardarFoto = { tipo, bitmap -> siniestrosVm.guardarFoto(id, tipo, bitmap) },
                onGuardarArchivo = { tipo, uri -> siniestrosVm.guardarArchivo(id, tipo, uri) }
            )
        }
        composable(
            route = Ruta.Editar.ruta,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            EditarSiniestroScreen(
                siniestro = siniestrosVm.obtener(id),
                onVolver = { navController.navigateUp() },
                onGuardar = { descripcion ->
                    if (siniestrosVm.actualizarDescripcion(id, descripcion)) {
                        navController.popBackStack(Ruta.MisSiniestros.ruta, false)
                    }
                }
            )
        }
    }
}
