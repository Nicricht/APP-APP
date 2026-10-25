package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.ui.style.estiloAltoBoton
import cl.duocuc.segurosbpo.ui.style.EstilosBotones
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme


@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.estiloAltoBoton(),
        shape = EstilosBotones.forma,
        colors = EstilosBotones.coloresSecundarios(),
        border = EstilosBotones.bordeSecundario(),
        contentPadding = EstilosBotones.relleno
    ) {
        Text(text = texto)
    }
}

@Preview(showBackground = true)
@Composable
internal fun BotonSecundarioPreview() {
    SegurosBpoTheme {
        var limpiezas by remember { mutableStateOf(0) }
        Column(Modifier.padding(16.dp)) {
            BotonSecundario(texto = "Limpiar", onClick = { limpiezas += 1 })
            Text("Limpiezas: " + limpiezas)
        }
    }
}
