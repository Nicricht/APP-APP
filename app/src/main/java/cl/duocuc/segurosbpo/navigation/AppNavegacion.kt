package cl.duocuc.segurosbpo.navigation

import android.os.Bundle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.annotation.DrawableRes
import cl.duocuc.segurosbpo.R
import androidx.navigation.compose.currentBackStackEntryAsState
import cl.duocuc.segurosbpo.ui.components.AccionesBarra
import cl.duocuc.segurosbpo.ui.components.LocalAccionesBarra
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duocuc.segurosbpo.ui.screens.SeleccionUsuarioScreen
import cl.duocuc.segurosbpo.ui.screens.AsignacionCasosScreen
import cl.duocuc.segurosbpo.ui.screens.AvisosScreen
import cl.duocuc.segurosbpo.ui.screens.CargaEquiposScreen
import cl.duocuc.segurosbpo.ui.screens.CatalogoScreen
import cl.duocuc.segurosbpo.ui.screens.LiquidadoresScreen
import cl.duocuc.segurosbpo.ui.screens.ConsultasScreen
import cl.duocuc.segurosbpo.ui.screens.EstadisticasAseguradoScreen
import cl.duocuc.segurosbpo.ui.screens.CasosLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.DisponiblesLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.DocumentosLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.EstadisticasLiquidadorScreen
import cl.duocuc.segurosbpo.ui.screens.GestionCasoScreen
import cl.duocuc.segurosbpo.ui.screens.DetalleSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EvidenciasScreen
import cl.duocuc.segurosbpo.ui.screens.HistorialSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.EditarSiniestroScreen
import cl.duocuc.segurosbpo.ui.screens.FormularioScreen
import cl.duocuc.segurosbpo.ui.screens.MisSiniestrosScreen
import cl.duocuc.segurosbpo.ui.screens.EditarPerfilScreen
import cl.duocuc.segurosbpo.ui.screens.PerfilCuentaScreen
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
private fun BarraInferior(
    rol: RolCuenta,
    rutaActual: String?,
    cantidadAvisos: Int,
    cantidadDisponibles: Int,
    cantidadPorAsignar: Int,
    onNavegar: (String) -> Unit,
    onPerfil: () -> Unit,
    onCambiarUsuario: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    NavigationBar {
        when (rol) {
            RolCuenta.ASEGURADO -> {
                OpcionInferior(
                    texto = "Inicio",
                    icono = R.drawable.ic_inicio,
                    seleccionada = rutaActual == Ruta.MisSiniestros.ruta,
                    onClick = { onNavegar(Ruta.MisSiniestros.ruta) }
                )
                OpcionInferior(
                    texto = "Reportar",
                    icono = R.drawable.ic_reportar,
                    seleccionada = rutaActual == Ruta.Reportar.ruta,
                    onClick = { onNavegar(Ruta.Reportar.ruta) }
                )
                OpcionInferior(
                    texto = "Estadísticas",
                    icono = R.drawable.ic_estadisticas,
                    seleccionada = rutaActual == Ruta.Estadisticas.ruta,
                    onClick = { onNavegar(Ruta.Estadisticas.ruta) }
                )
                OpcionInferior(
                    texto = "Avisos",
                    icono = R.drawable.ic_avisos,
                    seleccionada = rutaActual == Ruta.Avisos.ruta,
                    cantidad = cantidadAvisos,
                    onClick = { onNavegar(Ruta.Avisos.ruta) }
                )
            }
            RolCuenta.LIQUIDADOR -> {
                OpcionInferior(
                    texto = "Mis casos",
                    icono = R.drawable.ic_inicio,
                    seleccionada = rutaActual == Ruta.Liquidador.ruta,
                    onClick = { onNavegar(Ruta.Liquidador.ruta) }
                )
                OpcionInferior(
                    texto = "Disponibles",
                    icono = R.drawable.ic_reportar,
                    seleccionada = rutaActual == Ruta.Disponibles.ruta,
                    cantidad = cantidadDisponibles,
                    onClick = { onNavegar(Ruta.Disponibles.ruta) }
                )
                OpcionInferior(
                    texto = "Estadísticas",
                    icono = R.drawable.ic_estadisticas,
                    seleccionada = rutaActual == Ruta.EstadisticasLiquidador.ruta,
                    onClick = { onNavegar(Ruta.EstadisticasLiquidador.ruta) }
                )
            }
            RolCuenta.ANALISTA -> {
                OpcionInferior(
                    texto = "Por asignar",
                    icono = R.drawable.ic_inicio,
                    seleccionada = rutaActual == Ruta.Analista.ruta,
                    cantidad = cantidadPorAsignar,
                    onClick = { onNavegar(Ruta.Analista.ruta) }
                )
                OpcionInferior(
                    texto = "Equipo",
                    icono = R.drawable.ic_persona,
                    seleccionada = rutaActual == Ruta.Liquidadores.ruta,
                    onClick = { onNavegar(Ruta.Liquidadores.ruta) }
                )
                OpcionInferior(
                    texto = "Catálogos",
                    icono = R.drawable.ic_reportar,
                    seleccionada = rutaActual == Ruta.Catalogo.ruta,
                    onClick = { onNavegar(Ruta.Catalogo.ruta) }
                )
                OpcionInferior(
                    texto = "Carga",
                    icono = R.drawable.ic_estadisticas,
                    seleccionada = rutaActual == Ruta.CargaEquipos.ruta,
                    onClick = { onNavegar(Ruta.CargaEquipos.ruta) }
                )
            }
        }
        NavigationBarItem(
            selected = false,
            onClick = { menuAbierto = true },
            icon = {
                Icon(painterResource(R.drawable.ic_menu), contentDescription = null)
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mi perfil") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_persona),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuAbierto = false
                            onPerfil()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cambiar de usuario") },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_cambiar_usuario),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuAbierto = false
                            onCambiarUsuario()
                        }
                    )
                }
            },
            label = { Text("Menú", maxLines = 1) }
        )
    }
}

@Composable
private fun RowScope.OpcionInferior(
    texto: String,
    @DrawableRes icono: Int,
    seleccionada: Boolean,
    cantidad: Int = 0,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionada,
        onClick = onClick,
        icon = {
            if (cantidad > 0) {
                BadgedBox(badge = { Badge { Text(cantidad.toString()) } }) {
                    Icon(painterResource(icono), contentDescription = null)
                }
            } else {
                Icon(painterResource(icono), contentDescription = null)
            }
        },
        label = { Text(texto, maxLines = 1) }
    )
}

fun volverDelDetalle(navController: NavController) {
    if (!navController.popBackStack()) {
        navController.navigate(Ruta.MisSiniestros.ruta) {
            launchSingleTop = true
        }
    }
}

@Composable
fun AppNavegacion(
    temaOscuro: Boolean = false,
    onAlternarTema: () -> Unit = {}
) {
    val cuentaVm: CuentaSesionViewModel = viewModel()
    val sesion = cuentaVm.cuenta.collectAsStateWithLifecycle().value
    val usuarios = cuentaVm.usuarios.collectAsStateWithLifecycle().value
    if (sesion == null) {
        CompositionLocalProvider(
            LocalAccionesBarra provides AccionesBarra(
                temaOscuro = temaOscuro,
                onAlternarTema = onAlternarTema
            )
        ) {
            SeleccionUsuarioScreen(
                usuarios = usuarios,
                onSeleccionar = cuentaVm::seleccionar
            )
        }
        return
    }
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
    val seguimientosVencidos by siniestrosVm.seguimientosVencidos.collectAsStateWithLifecycle()
    val disponiblesLiquidador by siniestrosVm.disponiblesLiquidador.collectAsStateWithLifecycle()
    val gestionesLiquidador by siniestrosVm.gestionesLiquidador.collectAsStateWithLifecycle()
    val evidenciasLiquidador by siniestrosVm.evidenciasLiquidador.collectAsStateWithLifecycle()
    val consultas by siniestrosVm.consultas.collectAsStateWithLifecycle()
    val consultasLiquidador by siniestrosVm.consultasLiquidador.collectAsStateWithLifecycle()
    val evaluacion by siniestrosVm.evaluacion.collectAsStateWithLifecycle()
    val sinAsignar by siniestrosVm.sinAsignar.collectAsStateWithLifecycle()
    val cargaEquipos by siniestrosVm.cargaEquipos.collectAsStateWithLifecycle()
    val actualizadoEn by siniestrosVm.actualizadoEn.collectAsStateWithLifecycle()
    val archivados by siniestrosVm.archivados.collectAsStateWithLifecycle()
    val companias by siniestrosVm.companias.collectAsStateWithLifecycle()
    val tipos by siniestrosVm.tipos.collectAsStateWithLifecycle()
    val cargaPersonas by siniestrosVm.cargaPersonas.collectAsStateWithLifecycle()

    val equipoSesion = remember(sesion.id, sesion.equipo) {
        sesion.equipo?.let { runCatching { Liquidador.valueOf(it) }.getOrNull() }
    }

    LaunchedEffect(sesion.id) {
        when (sesion.rol) {
            RolCuenta.ASEGURADO -> siniestrosVm.seleccionarAsegurado(sesion.comoAsegurado())
            RolCuenta.LIQUIDADOR -> equipoSesion?.let {
                siniestrosVm.seleccionarLiquidador(it)
                siniestrosVm.identificarLiquidador(sesion.id, sesion.nombre)
            }
            RolCuenta.ANALISTA -> siniestrosVm.refrescarAnalista()
        }
    }

    val identidadLista = when (sesion.rol) {
        RolCuenta.ASEGURADO -> asegurado.name == sesion.id
        RolCuenta.LIQUIDADOR -> equipoSesion == null || liquidador.name == sesion.equipo
        RolCuenta.ANALISTA -> true
    }
    if (!identidadLista) {
        androidx.compose.material3.Text("Preparando la sesión")
        return
    }
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route
    val inicio = when (sesion.rol) {
        RolCuenta.ASEGURADO -> Ruta.MisSiniestros.ruta
        RolCuenta.LIQUIDADOR -> Ruta.Liquidador.ruta
        RolCuenta.ANALISTA -> Ruta.Analista.ruta
    }
    CompositionLocalProvider(
        LocalAccionesBarra provides AccionesBarra(
            temaOscuro = temaOscuro,
            onAlternarTema = onAlternarTema,
            onVolver = { navController.navigateUp() },
            puedeVolver = rutaActual != null && rutaActual != inicio
        )
    ) {
        Column(Modifier.fillMaxSize()) {
            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController,
                startDestination = inicio
            ) {
                composable(Ruta.Perfil.ruta) {
                    PerfilCuentaScreen(
                        cuenta = sesion,
                        onGuardarFoto = cuentaVm::actualizarFoto,
                        onEditar = { navController.navigate(Ruta.EditarPerfil.ruta) },
                        onVolver = { navController.navigateUp() },
                        onCerrarSesion = cuentaVm::cerrarSesion
                    )
                }
                composable(Ruta.EditarPerfil.ruta) {
                    EditarPerfilScreen(
                        cuenta = sesion,
                        onGuardar = cuentaVm::actualizarContacto,
                        onVolver = { navController.navigateUp() }
                    )
                }

                if (sesion.rol == RolCuenta.ANALISTA) {
                    composable(Ruta.Analista.ruta) {
                        AsignacionCasosScreen(
                            pendientes = sinAsignar,
                            carga = cargaEquipos,
                            equipos = Liquidador.entries.associateWith { equipo ->
                                usuarios.filter {
                                    it.rol == RolCuenta.LIQUIDADOR && it.equipo == equipo.name
                                }
                            },
                            cargaPersonas = cargaPersonas,
                            actualizadoEn = actualizadoEn,
                            onCargar = siniestrosVm::refrescarAnalista,
                            onAsignar = siniestrosVm::asignarCaso
                        )
                    }
                    composable(Ruta.CargaEquipos.ruta) {
                        CargaEquiposScreen(
                            carga = cargaEquipos,
                            pendientes = sinAsignar,
                            actualizadoEn = actualizadoEn,
                            onCargar = siniestrosVm::refrescarAnalista
                        )
                    }
                    composable(Ruta.Liquidadores.ruta) {
                        LiquidadoresScreen(
                            liquidadores = usuarios.filter {
                                it.rol == RolCuenta.LIQUIDADOR
                            },
                            carga = cargaPersonas,
                            onCrear = cuentaVm::crearLiquidador
                        )
                    }
                    composable(Ruta.Catalogo.ruta) {
                        CatalogoScreen(
                            companias = companias,
                            tipos = tipos,
                            onCrearCompania = siniestrosVm::crearCompania,
                            onCrearTipo = siniestrosVm::crearTipoSiniestro
                        )
                    }
                }

                if (sesion.rol == RolCuenta.ASEGURADO) {
                    composable(Ruta.MisSiniestros.ruta) {
                        MisSiniestrosScreen(
                            asegurado = asegurado,
                            fotoCuenta = sesion.foto,
                            actualizadoEn = actualizadoEn,
                            siniestros = siniestros,
                            busqueda = busqueda,
                            onBuscar = siniestrosVm::buscar,
                            onVerDetalle = { navController.navigate(Ruta.Detalle.crear(it)) },
                            onNuevoReporte = { navController.navigate(Ruta.Reportar.ruta) }
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
                            onAdjuntar = { navController.navigate(Ruta.Evidencias.crear(it)) },
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    composable(Ruta.Reportar.ruta) {
                        FormularioScreen(
                            aseguradoActual = asegurado,
                            onRegistroExitoso = siniestrosVm::registrar,
                            onPosibleDuplicado = siniestrosVm::buscarPosibleDuplicado,
                            onGuardarFoto = { tipo, bitmap ->
                                siniestrosVm.ultimoRegistrado?.let {
                                    siniestrosVm.guardarFoto(it, tipo, bitmap)
                                } ?: "No se pudo identificar el siniestro"
                            },
                            onGuardarArchivo = { tipo, uri ->
                                siniestrosVm.ultimoRegistrado?.let {
                                    siniestrosVm.guardarArchivo(it, tipo, uri)
                                } ?: "No se pudo identificar el siniestro"
                            },
                            onVolver = { navController.navigateUp() },
                            onVerMisSiniestros = {
                                val nuevo = siniestrosVm.ultimoRegistrado
                                if (nuevo == null) {
                                    navController.popBackStack(Ruta.MisSiniestros.ruta, false)
                                } else {
                                    navController.navigate(Ruta.Evidencias.crear(nuevo))
                                }
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
                            consultasPendientes = consultas.count { !it.respondida },
                            evaluacion = evaluacion,
                            onCargarSolicitudes = {
                                siniestrosVm.refrescarEvidencias(id)
                                siniestrosVm.refrescarConsultas(id)
                                siniestrosVm.refrescarEvaluacion(id)
                            },
                            onVolver = { volverDelDetalle(navController) },
                            onEditar = { navController.navigate(Ruta.Editar.crear(id)) },
                            onEvidencias = { navController.navigate(Ruta.Evidencias.crear(id)) },
                            onHistorial = { navController.navigate(Ruta.Historial.crear(id)) },
                            onConsultas = { navController.navigate(Ruta.Consultas.crear(id)) },
                            onDesistir = { motivo -> siniestrosVm.desistir(id, motivo) },
                            onGuardarEvaluacion = { nota, comentario ->
                                siniestrosVm.guardarEvaluacion(id, nota, comentario)
                            }
                        )
                    }
                    composable(
                        route = Ruta.Consultas.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        ConsultasScreen(
                            siniestro = siniestrosVm.obtener(id),
                            consultas = consultas,
                            onCargar = { siniestrosVm.refrescarConsultas(id) },
                            onEnviar = { pregunta -> siniestrosVm.enviarConsulta(id, pregunta) },
                            onVolver = { navController.navigateUp() }
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
                            onCargarImagen = { evidenciaId ->
                                siniestrosVm.cargarImagen(id, evidenciaId)
                            },
                            onCargarMiniatura = { evidenciaId ->
                                siniestrosVm.cargarMiniatura(id, evidenciaId)
                            },
                            onGuardarFoto = { tipo, bitmap ->
                                siniestrosVm.guardarFoto(id, tipo, bitmap)
                            },
                            onGuardarArchivo = { tipo, uri ->
                                siniestrosVm.guardarArchivo(id, tipo, uri)
                            },
                            onEliminar = { evidenciaId ->
                                siniestrosVm.eliminarEvidencia(id, evidenciaId)
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

                if (sesion.rol == RolCuenta.LIQUIDADOR) {
                    composable(Ruta.Liquidador.ruta) {
                        CasosLiquidadorScreen(
                            liquidador = liquidador,
                            responsable = sesion.nombre,
                            casos = casosLiquidador,
                            archivados = archivados,
                            seguimientosVencidos = seguimientosVencidos,
                            onCargarArchivados = siniestrosVm::refrescarArchivados,
                            onArchivar = siniestrosVm::archivarCaso,
                            onRestaurar = siniestrosVm::restaurarCaso,
                            onGestionar = { navController.navigate(Ruta.GestionCaso.crear(it)) }
                        )
                    }
                    composable(
                        Ruta.GestionCaso.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        GestionCasoScreen(
                            caso = casosLiquidador.firstOrNull { it.id == id },
                            gestiones = gestionesLiquidador,
                            solicitudes = solicitudesLiquidador,
                            consultas = consultasLiquidador,
                            seguimientoVencido = id in seguimientosVencidos,
                            onCargar = { siniestrosVm.cargarGestionesLiquidador(id) },
                            onRegistrarGestion = siniestrosVm::registrarGestionLiquidador,
                            onSolicitarDocumento = siniestrosVm::solicitarDocumentoLiquidador,
                            onAvanzar = siniestrosVm::avanzarLiquidador,
                            onCerrarAnticipado = siniestrosVm::cerrarAnticipadoLiquidador,
                            onResponderConsulta = { consultaId, respuesta ->
                                siniestrosVm.responderConsulta(id, consultaId, respuesta)
                            },
                            onMarcarRecibida = { solicitudId ->
                                siniestrosVm.marcarSolicitudRecibida(id, solicitudId)
                            },
                            onAnularSolicitud = { solicitudId ->
                                siniestrosVm.anularSolicitud(id, solicitudId)
                            },
                            onVerDocumentos = {
                                navController.navigate(Ruta.DocumentosLiquidador.crear(it))
                            },
                            onVolver = { navController.navigateUp() }
                        )
                    }
                    composable(Ruta.Disponibles.ruta) {
                        DisponiblesLiquidadorScreen(
                            liquidador = liquidador,
                            disponibles = disponiblesLiquidador,
                            onTomarCaso = siniestrosVm::tomarCasoLiquidador
                        )
                    }
                    composable(
                        Ruta.DocumentosLiquidador.ruta,
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { entrada ->
                        val id = idDesdeArgumentos(entrada.arguments)
                        DocumentosLiquidadorScreen(
                            siniestro = casosLiquidador.firstOrNull { it.id == id },
                            evidencias = evidenciasLiquidador,
                            onVolver = { navController.navigateUp() },
                            onCargar = { siniestrosVm.cargarEvidenciasLiquidador(id) },
                            onCargarImagen = { evidenciaId ->
                                siniestrosVm.cargarImagenLiquidador(id, evidenciaId)
                            },
                            onCargarMiniatura = { evidenciaId ->
                                siniestrosVm.cargarMiniaturaLiquidador(id, evidenciaId)
                            },
                            onDescargar = { evidenciaId, destino ->
                                siniestrosVm.descargarDocumentoLiquidador(id, evidenciaId, destino)
                            },
                            onGuardarFoto = { tipo, bitmap ->
                                siniestrosVm.guardarFotoLiquidador(id, tipo, bitmap)
                            },
                            onGuardarArchivo = { tipo, uri ->
                                siniestrosVm.guardarArchivoLiquidador(id, tipo, uri)
                            },
                            onEliminar = { evidenciaId ->
                                siniestrosVm.eliminarEvidenciaLiquidador(id, evidenciaId)
                            }
                        )
                    }
                    composable(Ruta.EstadisticasLiquidador.ruta) {
                        EstadisticasLiquidadorScreen(
                            liquidador = liquidador,
                            responsable = sesion.nombre,
                            casos = casosLiquidador,
                            disponibles = disponiblesLiquidador,
                            seguimientosVencidos = seguimientosVencidos
                        )
                    }
                }
            }
            BarraInferior(
                rol = sesion.rol,
                rutaActual = rutaActual,
                cantidadAvisos = contarAvisosNoLeidos(avisos),
                cantidadDisponibles = disponiblesLiquidador.size,
                cantidadPorAsignar = sinAsignar.size,
                onNavegar = { ruta ->
                    navController.navigate(ruta) {
                        popUpTo(inicio)
                        launchSingleTop = true
                    }
                },
                onPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onCambiarUsuario = cuentaVm::cerrarSesion
            )
        }
    }
}
