package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.ui.components.CodigoSiniestro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasosLiquidadorScreen(
    liquidador: Liquidador,
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

    LaunchedEffect(liquidador) { onCargarArchivados() }

    fun mostrar(mensaje: String) {
        scope.launch { aviso.showSnackbar(mensaje) }
    }

    Scaffold(
        topBar = { BarraSuperior("Mis casos") },
        snackbarHost = { SnackbarHost(aviso) }
    ) { margen ->
        Column(
            Modifier.fillMaxSize().padding(margen).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Equipo de " + liquidador.equipo, style = MaterialTheme.typography.titleLarge)
            Text("Responsable: " + liquidador.nombre, style = MaterialTheme.typography.bodySmall)

            if (archivados.isNotEmpty()) {
                FilterChip(
                    selected = viendoArchivados,
                    onClick = { viendoArchivados = !viendoArchivados },
                    label = { Text("Archivados (" + archivados.size + ")") }
                )
            }

            val visibles = if (viendoArchivados) archivados else casos
            if (visibles.isEmpty()) {
                Text(
                    if (viendoArchivados) "No hay casos archivados."
                    else "No hay casos activos en el equipo."
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
    }
}
