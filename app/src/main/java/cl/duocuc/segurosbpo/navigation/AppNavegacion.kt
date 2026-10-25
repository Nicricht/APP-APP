package cl.duocuc.segurosbpo.navigation

import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
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
import cl.duocuc.segurosbpo.R

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

/** Regresa al origen normal o lleva al listado si un detalle llegó sin historial. */
fun volverDelDetalle(navController: NavController) {
    if (!navController.popBackStack()) {
        navController.navigate(Ruta.MisSiniestros.ruta) {
            launchSingleTop = true
        }
    }
}

@Composable
fun AppNavegacion() {
    val cuentaVm: CuentaSesionViewModel = viewModel()
    val sesion = cuentaVm.cuenta.collectAsStateWithLifecycle().value
    if (sesion == null) {
        AccesoScreen(
            perfiles = cuentaVm.perfilesDisponibles,
            onElegirPerfil = cuentaVm::seleccionarPerfil
        )
        return
    }
    val navController = rememberNavController()
    val siniestrosVm: SiniestrosViewModel = viewModel()
    val asegurado by siniestrosVm.asegurado.collectAsStateWithLifecycle()
    val siniestros by siniestrosVm.siniestros.collectAsStateWithLifecycle()
    val estadisticasAsegurado by siniestrosVm.estadisticasAsegurado.collectAsStateWithLifecycle()
    val busqueda by siniestrosVm.busqueda.collectAsStateWithLifecycle()
    val evidencias by siniestrosVm.evidencias.collectAsStateWithLifecycle()
    val evidenciasLiquidador by siniestrosVm.evidenciasLiquidador.collectAsStateWithLifecycle()
    val solicitudes by siniestrosVm.solicitudes.collectAsStateWithLifecycle()
    val solicitudesLiquidador by siniestrosVm.solicitudesLiquidador.collectAsStateWithLifecycle()
    val avisos by siniestrosVm.avisos.collectAsStateWithLifecycle()
    val gestiones by siniestrosVm.gestiones.collectAsStateWithLifecycle()
    val liquidador by siniestrosVm.liquidador.collectAsStateWithLifecycle()
    val casosLiquidador by siniestrosVm.casosLiquidador.collectAsStateWithLifecycle()
    val seguimientosVencidos by siniestrosVm.seguimientosVencidos.collectAsStateWithLifecycle()
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
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
    fun navegarDesdeMenu(ruta: String) {
        navController.navigate(ruta) { launchSingleTop = true }
        drawerScope.launch { drawerState.close() }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
              Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.mi_siniestro_logo),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp)
                    )
                    Column {
                        Text("Seguros BPO")
                        Text(sesion.nombre)
                    }
                }
                HorizontalDivider()
                if (sesion.rol == RolCuenta.ASEGURADO) {
                    NavigationDrawerItem(
                        label = { Text("Panel del asegurado") },
                        icon = { Icon(painterResource(R.drawable.ic_inicio), contentDescription = null) },
                        selected = rutaActual == Ruta.MisSiniestros.ruta,
                        onClick = { navegarDesdeMenu(Ruta.MisSiniestros.ruta) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Reportar siniestro") },
                        icon = { Icon(painterResource(R.drawable.ic_reportar), contentDescription = null) },
                        selected = rutaActual == Ruta.Reportar.ruta,
                        onClick = { navegarDesdeMenu(Ruta.Reportar.ruta) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Mis estadísticas") },
                        icon = { Icon(painterResource(R.drawable.ic_estadisticas), contentDescription = null) },
                        selected = rutaActual == Ruta.Estadisticas.ruta,
                        onClick = { navegarDesdeMenu(Ruta.Estadisticas.ruta) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Avisos" + if (contarAvisosNoLeidos(avisos) > 0)
                            " (${contarAvisosNoLeidos(avisos)})" else "") },
                        icon = { Icon(painterResource(R.drawable.ic_avisos), contentDescription = null) },
                        selected = rutaActual == Ruta.Avisos.ruta,
                        onClick = { navegarDesdeMenu(Ruta.Avisos.ruta) }
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text("Gestión de siniestros") },
                        icon = { Icon(painterResource(R.drawable.ic_casos), contentDescription = null) },
                        selected = rutaActual == Ruta.Liquidador.ruta,
                        onClick = { navegarDesdeMenu(Ruta.Liquidador.ruta) }
                    )
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Mi perfil") },
                    icon = { Icon(painterResource(R.drawable.ic_persona), contentDescription = null) },
                    selected = rutaActual == Ruta.Perfil.ruta,
                    onClick = { navegarDesdeMenu(Ruta.Perfil.ruta) }
                )
                NavigationDrawerItem(
                    label = { Text("Cambiar perfil") },
                    icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
                    selected = false,
                    onClick = cuentaVm::cerrarSesion
                )
              }
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
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
                siniestroDestacado = estadisticasAsegurado.maxByOrNull { it.id },
                permitirCambioAsegurado = false,
                permitirGestor = false,
                onCerrarSesion = cuentaVm::cerrarSesion,
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onAbrirMenu = { drawerScope.launch { drawerState.open() } }
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
                onAdjuntarDocumento = { navController.navigate(Ruta.Evidencias.crear(it)) },
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
                seguimientosVencidos = seguimientosVencidos,
                onTomarCaso = siniestrosVm::tomarCasoLiquidador,
                gestiones = gestionesLiquidador,
                solicitudes = solicitudesLiquidador,
                evidencias = evidenciasLiquidador,
                onAbrirEvidencia = siniestrosVm::obtenerArchivoLiquidador,
                onSolicitarDocumento = siniestrosVm::solicitarDocumentoLiquidador,
                onSeleccionarLiquidador = siniestrosVm::seleccionarLiquidador,
                onCargarGestiones = siniestrosVm::cargarGestionesLiquidador,
                onRegistrarGestion = siniestrosVm::registrarGestionLiquidador,
                onAvanzar = siniestrosVm::avanzarLiquidador,
                onCerrarAnticipado = siniestrosVm::cerrarAnticipadoLiquidador,
                onVolver = { navController.navigateUp() },
                permitirElegirEquipo = false,
                onCerrarSesion = cuentaVm::cerrarSesion,
                onVerPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onAbrirMenu = { drawerScope.launch { drawerState.open() } }
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
                onVolver = { volverDelDetalle(navController) },
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
                onAbrirArchivo = { evidenciaId -> siniestrosVm.obtenerArchivoAsegurado(id, evidenciaId) },
                onGuardarFoto = { tipo, detalle, bitmap ->
                    siniestrosVm.guardarFoto(id, tipo, detalle, bitmap)
                },
                onGuardarArchivo = { tipo, detalle, uri ->
                    siniestrosVm.guardarArchivo(id, tipo, detalle, uri)
                }
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
            val destinosBarra = if (sesion.rol == RolCuenta.ASEGURADO) listOf(
                Triple(Ruta.MisSiniestros.ruta, "Inicio", R.drawable.ic_inicio),
                Triple(Ruta.Reportar.ruta, "Reportar", R.drawable.ic_reportar),
                Triple(Ruta.Avisos.ruta, "Avisos", R.drawable.ic_avisos),
                Triple(Ruta.Perfil.ruta, "Perfil", R.drawable.ic_persona)
            ) else listOf(
                Triple(Ruta.Liquidador.ruta, "Casos", R.drawable.ic_casos),
                Triple(Ruta.Perfil.ruta, "Perfil", R.drawable.ic_persona)
            )
            if (destinosBarra.any { it.first == rutaActual }) {
                NavigationBar {
                    destinosBarra.forEach { (ruta, nombre, icono) ->
                        NavigationBarItem(
                            selected = rutaActual == ruta,
                            onClick = {
                                navController.navigate(ruta) {
                                    launchSingleTop = true
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(icono), contentDescription = null) },
                            label = { Text(nombre) }
                        )
                    }
                }
            }
        }
    }
}
