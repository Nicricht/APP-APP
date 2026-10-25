package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.style.EstiloCampo
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme


@Composable
fun CampoContrasena(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    visible: Boolean,
    onAlternarVisible: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_candado),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
        },
        trailingIcon = {
            IconButton(onClick = onAlternarVisible) {
                Icon(
                    painter = painterResource(
                        if (visible) R.drawable.ic_ojo_tachado else R.drawable.ic_ojo
                    ),
                    contentDescription = stringResource(
                        if (visible) R.string.cd_ocultar_contraseña else R.string.cd_mostrar_contraseña
                    ),
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = EstiloCampo.forma,
        colors = EstiloCampo.colores()
    )
}

@Preview(showBackground = true)
@Composable
internal fun CampoContrasenaPreview() {
    SegurosBpoTheme {
        var clave by remember { mutableStateOf("secreto") }
        var visible by remember { mutableStateOf(false) }
        Column(Modifier.padding(16.dp)) {
            CampoContrasena(
                valor = clave,
                onValorCambia = { clave = it },
                etiqueta = "Contraseña",
                visible = visible,
                onAlternarVisible = { visible = !visible }
            )
            Text("Caracteres: " + clave.length)
        }
    }
}
