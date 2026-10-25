package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.ui.style.EstiloTarjeta
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme


@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EstiloTarjeta.forma,
        colors = EstiloTarjeta.colores(),
        elevation = EstiloTarjeta.elevacion()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                contenido()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun TarjetaSeccionPreview() {
    SegurosBpoTheme {
        TarjetaSeccion(titulo = "Sobre mí", modifier = Modifier.padding(16.dp)) {
            Text("Cualquier contenido puede ir aquí dentro.")
        }
    }
}
