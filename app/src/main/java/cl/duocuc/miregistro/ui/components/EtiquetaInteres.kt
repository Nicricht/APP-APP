package cl.duocuc.miregistro.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

// =====================================================================
//  EtiquetaInteres.kt  —  "Chip" o etiqueta con forma de píldora.
//  ---------------------------------------------------------------------
//  Surface = el contenedor más simple de Material: pinta un fondo con
//  forma y color, y pone su contenido encima. (Card está hecha con Surface.)
//  En CSS: <span class="chip"> con background, border-radius:50px y padding.
// =====================================================================

@Composable
fun EtiquetaInteres(
    texto: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),                        // 50 % = píldora
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EtiquetaInteresPreview() {
    MiRegistroTheme {
        EtiquetaInteres(texto = "Kotlin", modifier = Modifier.padding(16.dp))
    }
}
