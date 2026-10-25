package cl.duocuc.segurosbpo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.ui.components.AvatarCuenta
import cl.duocuc.segurosbpo.ui.components.BarraSuperior
import cl.duocuc.segurosbpo.model.ValidadorRut
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.etiquetaEquipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionUsuarioScreen(
    usuarios: List<CuentaLocal>,
    onSeleccionar: (String) -> Unit
) {
    val asegurados = usuarios.filter { it.rol == RolCuenta.ASEGURADO }
    val liquidadores = usuarios.filter { it.rol == RolCuenta.LIQUIDADOR }
    val analistas = usuarios.filter { it.rol == RolCuenta.ANALISTA }

    Scaffold(topBar = { BarraSuperior("Seguros BPO") }) { margen ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margen).imePadding()
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Elige quién eres", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Selecciona tu cuenta para continuar.",
                style = MaterialTheme.typography.bodySmall
            )

            if (asegurados.isNotEmpty()) {
                Text("Personas aseguradas", style = MaterialTheme.typography.titleMedium)
                asegurados.forEach { cuenta ->
                    TarjetaUsuario(
                        nombre = cuenta.nombre,
                        detalle = "RUT: " + rutOculto(cuenta),
                        foto = cuenta.foto,
                        onClick = { onSeleccionar(cuenta.id) }
                    )
                }
            }

            if (liquidadores.isNotEmpty()) {
                Text("Liquidadores", style = MaterialTheme.typography.titleMedium)
                liquidadores.forEach { cuenta ->
                    TarjetaUsuario(
                        nombre = cuenta.nombre,
                        detalle = "Equipo de " + etiquetaEquipo(cuenta.equipo),
                        foto = cuenta.foto,
                        onClick = { onSeleccionar(cuenta.id) }
                    )
                }
            }

            if (analistas.isNotEmpty()) {
                Text(
                    "Administración de siniestros",
                    style = MaterialTheme.typography.titleMedium
                )
                analistas.forEach { cuenta ->
                    TarjetaUsuario(
                        nombre = cuenta.nombre,
                        detalle = "Analista de asignaciones",
                        foto = cuenta.foto,
                        onClick = { onSeleccionar(cuenta.id) }
                    )
                }
            }

            if (usuarios.isEmpty()) {
                Text(
                    "No hay usuarios disponibles.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun rutOculto(cuenta: CuentaLocal): String = ValidadorRut.ocultar(cuenta.rut)

@Composable
private fun TarjetaUsuario(
    nombre: String,
    detalle: String,
    foto: ByteArray? = null,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCuenta(foto = foto, descripcion = nombre, tamano = 44.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(nombre, style = MaterialTheme.typography.titleMedium)
                Text(detalle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
