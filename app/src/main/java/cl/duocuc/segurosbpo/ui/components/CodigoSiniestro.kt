package cl.duocuc.segurosbpo.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import kotlinx.coroutines.delay

@Composable
fun CodigoSiniestro(
    numero: String,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.titleMedium
) {
    val contexto = LocalContext.current
    var copiado by remember(numero) { mutableStateOf(false) }

    if (copiado) {
        LaunchedEffect(numero) {
            delay(1600)
            copiado = false
        }
    }

    Row(
        modifier = modifier.clickable {
            copiarCodigo(contexto, numero)
            copiado = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(numero, style = estilo)
        Icon(
            painter = painterResource(R.drawable.ic_copiar),
            contentDescription = "Copiar el número " + numero,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        if (copiado) {
            Text(
                "Copiado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun copiarCodigo(contexto: Context, numero: String) {
    val portapapeles = contexto.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return
    portapapeles.setPrimaryClip(ClipData.newPlainText("Número de siniestro", numero))
}
