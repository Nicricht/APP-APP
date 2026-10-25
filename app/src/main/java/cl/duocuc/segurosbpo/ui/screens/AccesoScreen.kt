package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta

/** Identificación simulada exigida por S-02. No hay alta ni autenticación real. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccesoScreen(
    perfiles: List<CuentaLocal>,
    onElegirPerfil: (String) -> String?
) {
    var rol by rememberSaveable { mutableStateOf(RolCuenta.ASEGURADO) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val visibles = perfiles.filter { it.rol == rol }

    Scaffold(topBar = { TopAppBar(title = { Text("Seguros BPO") }) }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.mi_siniestro_logo),
                contentDescription = "Logo de Seguros BPO",
                modifier = Modifier.size(76.dp).align(Alignment.CenterHorizontally)
            )
            Text("¿Cómo quieres ingresar?", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Elige un perfil de prueba para conocer y gestionar sus siniestros.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Demostración académica", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Los perfiles son simulados. No necesitas crear una cuenta, " +
                            "escribir contraseñas ni ingresar información personal.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text("Selecciona un rol", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = rol == RolCuenta.ASEGURADO,
                    onClick = { rol = RolCuenta.ASEGURADO; mensaje = null },
                    label = { Text("Asegurado") }
                )
                FilterChip(
                    selected = rol == RolCuenta.LIQUIDADOR,
                    onClick = { rol = RolCuenta.LIQUIDADOR; mensaje = null },
                    label = { Text("Liquidador") }
                )
            }

            Text(
                if (rol == RolCuenta.ASEGURADO) "Personas aseguradas de prueba"
                else "Equipos de liquidación",
                style = MaterialTheme.typography.titleMedium
            )

            visibles.forEach { perfil ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(perfil.nombre, style = MaterialTheme.typography.titleMedium)
                        val subtitulo = if (perfil.rol == RolCuenta.ASEGURADO) {
                            if (perfil.id.startsWith("USR_")) "Perfil conservado de una versión anterior"
                            else "Consultar y reportar siniestros"
                        } else {
                            val especialidad = Liquidador.entries
                                .firstOrNull { it.name == perfil.equipo }?.equipo.orEmpty()
                            "Revisar los casos de $especialidad"
                        }
                        Text(subtitulo, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = { mensaje = onElegirPerfil(perfil.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Entrar como ${perfil.nombre.substringBefore(' ')}")
                        }
                    }
                }
            }

            if (visibles.isEmpty()) {
                Text("No hay perfiles disponibles para este rol.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text(
                "Utiliza solamente casos, imágenes y documentos ficticios. " +
                    "El cambio de perfil funciona en este mismo dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
