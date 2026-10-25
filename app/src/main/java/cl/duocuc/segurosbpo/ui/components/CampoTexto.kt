package cl.duocuc.segurosbpo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.style.EstiloCampo
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme


@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    multilinea: Boolean = false,
    onPulsar: (() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        readOnly = onPulsar != null,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
        },
        isError = error != null,
        supportingText = if (error != null) { { Text(error) } } else null,
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        maxLines = if (multilinea) 5 else 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = if (multilinea) ImeAction.Default else ImeAction.Next
        ),
        shape = EstiloCampo.forma,
        colors = EstiloCampo.colores()
    )
    if (onPulsar != null) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onPulsar)
        )
    }
    }
}

@Preview(showBackground = true)
@Composable
internal fun CampoTextoPreview() {
    SegurosBpoTheme {
        var correo by remember { mutableStateOf("ana@") }
        CampoTexto(
            valor = correo,
            onValorCambia = { correo = it },
            etiqueta = "Correo electrónico",
            icono = R.drawable.ic_correo,
            error = if ("@" in correo && correo.endsWith(".cl")) null else "Ingresa un correo válido",
            modifier = Modifier.padding(16.dp)
        )
    }
}
