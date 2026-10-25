package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.coincideCon
import cl.duocuc.segurosbpo.ui.components.AvisoSuperior
import cl.duocuc.segurosbpo.ui.components.BuscadorCasos
import cl.duocuc.segurosbpo.ui.components.FiltroEtapas
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasosLiquidadorScreen(
    liquidador: Liquidador,
    responsable: String,
    casos: List<Siniestro>,
    archivados: List<Siniestro>,
    seguimientosVencidos: Set<Long>,
    onCargarArchivados: () -> Unit,
    onArchivar: (Long) -> String?,
    onRestaurar: (Long) -> String?,
    onGestionar: (Long) -> Unit
) {
    val aviso = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var viendoArchivados by rememberSaveable(liquidador) { mutableStateOf(false) }
    var busqueda by rememberSaveable(liquidador) { mutableStateOf("") }
    var etapaFiltro by rememberSaveable(liquidador) { mutableStateOf<String?>(null) }

    LaunchedEffect(liquidador) { onCargarArchivados() }

    fun mostrar(mensaje: String) {
        scope.launch { aviso.showSnackbar(mensaje) }
    }

    Scaffold(
        topBar = { BarraSuperior("Mis casos") }
    ) { margen ->
        Box(Modifier.fillMaxSize().padding(margen).imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Equipo de " + liquidador.equipo, style = MaterialTheme.typography.titleLarge)
            Text("Responsable: " + responsable, style = MaterialTheme.typography.bodySmall)

            val base = if (viendoArchivados) archivados else casos
            val etapasPresentes = EtapaSiniestro.entries.filter { etapa ->
                base.any { it.etapa == etapa }
            }

            BuscadorCasos(texto = busqueda, onTexto = { busqueda = it })
            FiltroEtapas(
                etapas = etapasPresentes,
                seleccionada = etapaFiltro?.let { nombre ->
                    etapasPresentes.firstOrNull { it.name == nombre }
                },
                onSeleccionar = { etapaFiltro = it?.name }
            ) {
                if (archivados.isNotEmpty()) {
                    FilterChip(
                        selected = viendoArchivados,
                        onClick = {
                            viendoArchivados = !viendoArchivados
                            etapaFiltro = null
                        },
                        label = { Text("Archivados (" + archivados.size + ")") }
                    )
                }
            }

            val visibles = base
                .filter { it.coincideCon(busqueda) }
                .filter { etapaFiltro == null || it.etapa.name == etapaFiltro }

            Text(
                "Mostrando " + visibles.size + " de " + base.size + " caso(s)",
                style = MaterialTheme.typography.bodySmall
            )

            if (visibles.isEmpty()) {
                Text(
                    when {
                        base.isEmpty() && viendoArchivados -> "No hay casos archivados."
                        base.isEmpty() -> "No hay casos activos en el equipo."
                        else -> "Ningún caso coincide con la búsqueda o el filtro."
                    }
                )
            }
            visibles.forEach { siniestro ->
                Card(
                    onClick = { onGestionar(siniestro.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CodigoSiniestro(siniestro.numero)
                        Text(siniestro.tipo.etiqueta)
                        Text(
                            "Etapa: " + siniestro.etapa.etiqueta,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (siniestro.id in seguimientosVencidos) {
                            Text(
                                "Seguimiento pendiente",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (viendoArchivados) {
                            TextButton(onClick = {
                                mostrar(onRestaurar(siniestro.id) ?: "Caso restaurado")
                            }) { Text("Restaurar") }
                        } else if (siniestro.etapa == EtapaSiniestro.CERRADO) {
                            TextButton(onClick = {
                                mostrar(onArchivar(siniestro.id) ?: "Caso archivado")
                            }) { Text("Archivar") }
                        }
                    }
                }
            }
        }
        AvisoSuperior(aviso)
        }
    }
}
