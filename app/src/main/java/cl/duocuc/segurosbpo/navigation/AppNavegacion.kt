package cl.duocuc.segurosbpo.navigation

import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.segurosbpo.ui.screens.AccesoScreen
import cl.duocuc.segurosbpo.ui.screens.AvisosScreen
import cl.duocuc.segurosbpo.ui.screens.EstadisticasAseguradoScreen
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import cl.duocuc.segurosbpo.ui.screens.HistorialSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EditarSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import cl.duocuc.segurosbpo.ui.screens.PerfilCuentaScreen
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.comoAsegurado
import cl.duocuc.segurosbpo.viewmodel.CuentaSesionViewModel
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
    val cuentaVm: CuentaSesionViewModel = viewModel()
    val cuenta by cuentaVm.cuenta.collectAsStateWithLifecycle()
    if (cuenta == null) {
        AccesoScreen(
            onIniciar = cuentaVm::iniciar,
            onRegistrar = cuentaVm::registrar,
            onDemoLiquidador = cuentaVm::accederLiquidadorDemo,
            onDemoAsegurado = cuentaVm::accederAseguradoDemo
        )
        return
    }
    val sesion = cuenta ?: return
    val navController = rememberNavController()
    val siniestrosVm: SiniestrosViewModel = viewModel()
    val asegurado by siniestrosVm.asegurado.collectAsStateWithLifecycle()
    val siniestros by siniestrosVm.siniestros.collectAsStateWithLifecycle()
    val estadisticasAsegurado by siniestrosVm.estadisticasAsegurado.collectAsStateWithLifecycle()
    val busqueda by siniestrosVm.busqueda.collectAsStateWithLifecycle()
    val evidencias by siniestrosVm.evidencias.collectAsStateWithLifecycle()
    val solicitudes by siniestrosVm.solicitudes.collectAsStateWithLifecycle()
    val solicitudesLiquidador by siniestrosVm.solicitudesLiquidador.collectAsStateWithLifecycle()
    val avisos by siniestrosVm.avisos.collectAsStateWithLifecycle()
    val gestiones by siniestrosVm.gestiones.collectAsStateWithLifecycle()
    val liquidador by siniestrosVm.liquidador.collectAsStateWithLifecycle()
    val casosLiquidador by siniestrosVm.casosLiquidador.collectAsStateWithLifecycle()
    val disponiblesLiquidador by siniestrosVm.disponiblesLiquidador.collectAsStateWithLifecycle()
    val gestionesLiquidador by siniestrosVm.gestionesLiquidador.collectAsStateWithLifecycle()

    LaunchedEffect(sesion.id) {
        if (sesion.rol == RolCuenta.ASEGURADO) {
            siniestrosVm.seleccionarAsegurado(sesion.comoAsegurado())
        } else {
            siniestrosVm.seleccionarLiquidador(Liquidador.valueOf(sesion.equipo!!))
        }
    }

    val identidadLista = if (sesion.rol == RolCuenta.ASEGURADO) {
        asegurado.name == sesion.id
    } else {
        liquidador.name == sesion.equipo
    }
    if (!identidadLista) {
        androidx.compose.material3.Text("Preparando la sesión")
        return
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Seguros BPO · " + sesion.nombre)
                if (sesion.rol == RolCuenta.ASEGURADO) {
                    NavigationDrawerItem(
                        label = { Text("Panel del asegurado") },
                        selected = false,
                        onClick = {
                            navController.navigate(Ruta.MisSiniestros.ruta)
                            drawerScope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Reportar siniestro") },
                        selected = false,
                        onClick = {
                            navController.navigate(Ruta.Reportar.ruta)
                            drawerScope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Ver estadísticas") },
                        selected = false,
                        onClick = {
                            navController.navigate(Ruta.Estadisticas.ruta)
                            drawerScope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Centro de avisos") },
                        selected = false,
                        onClick = {
                            navController.navigate(Ruta.Avisos.ruta)
                            drawerScope.launch { drawerState.close() }
                        }
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Panel de trabajo") },
                        selected = false,
                        onClick = {
                            navController.navigate(Ruta.Liquidador.ruta)
                            drawerScope.launch { drawerState.close() }
                        }
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Cuenta y perfil") },
                    selected = false,
                    onClick = {
                        navController.navigate(Ruta.Perfil.ruta)
                        drawerScope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Salir de la cuenta") },
                    selected = false,
                    onClick = cuentaVm::cerrarSesion
                )
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            TextButton(onClick = { drawerScope.launch { drawerState.open() } }) {
                Text("Abrir menú")
            }
            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController,
                startDestination = if (sesion.rol == RolCuenta.ASEGURADO)
                    Ruta.MisSiniestros.ruta else Ruta.Liquidador.ruta
            ) {
        composable(Ruta.Perfil.ruta) {
            PerfilCuentaScreen(
                cuenta = sesion,
                onGuardarTelefono = cuentaVm::actualizarTelefono,
                onGuardarFoto = cuentaVm::actualizarFoto,
                onVolver = { navController.navigateUp() },
                onCerrarSesion = cuentaVm::cerrarSesion
            )
        }
        if (sesion.rol == RolCuenta.ASEGURADO) {
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
                onVerDetalle = { navController.navigate(Ruta.Detalle.crear(it)) },
                onVerEstadisticas = { navController.navigate(Ruta.Estadisticas.ruta) },
                permitirCambioAsegurado = false,
                permitirGestor = false,
                onCerrarSesion = cuentaVm::cerrarSesion,
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) }
            )
        }
        composable(Ruta.Estadisticas.ruta) {
            EstadisticasAseguradoScreen(
                siniestros = estadisticasAsegurado,
                onVolver = { navController.navigateUp() }
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
        }
        if (sesion.rol == RolCuenta.LIQUIDADOR) {
        composable(Ruta.Liquidador.ruta) {
            CasosLiquidadorScreen(
                liquidador = liquidador,
                casos = casosLiquidador,
                disponibles = disponiblesLiquidador,
                onTomarCaso = siniestrosVm::tomarCasoLiquidador,
                gestiones = gestionesLiquidador,
                solicitudes = solicitudesLiquidador,
                onSolicitarDocumento = siniestrosVm::solicitarDocumentoLiquidador,
                onSeleccionarLiquidador = siniestrosVm::seleccionarLiquidador,
                onCargarGestiones = siniestrosVm::cargarGestionesLiquidador,
                onRegistrarGestion = siniestrosVm::registrarGestionLiquidador,
                onAvanzar = siniestrosVm::avanzarLiquidador,
                onCerrarAnticipado = siniestrosVm::cerrarAnticipadoLiquidador,
                onVolver = { navController.navigateUp() },
                permitirElegirEquipo = false,
                onCerrarSesion = cuentaVm::cerrarSesion,
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) }
            )
        }
        }
        if (sesion.rol == RolCuenta.ASEGURADO) {
        composable(Ruta.Reportar.ruta) {
            FormularioScreen(
                aseguradoActual = asegurado,
                onRegistroExitoso = siniestrosVm::registrar,
                onPosibleDuplicado = siniestrosVm::buscarPosibleDuplicado,
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
                onVolver = {
                    if (!navController.popBackStack(Ruta.MisSiniestros.ruta, false)) {
                        navController.navigate(Ruta.MisSiniestros.ruta)
                    }
                },
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
        }
    }
}
