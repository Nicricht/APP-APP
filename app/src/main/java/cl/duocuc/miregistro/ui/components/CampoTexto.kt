package cl.duocuc.miregistro.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.style.EstiloCampo
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

// =====================================================================
//  CampoTexto.kt  —  CAMPO DE TEXTO REUTILIZABLE (el <input> de HTML)
//  ---------------------------------------------------------------------
//  IMPORTANTE: en Compose un campo NO guarda su propio texto.
//   - "valor" es lo que se muestra (viene del estado del ViewModel).
//   - "onValorCambia" avisa cada vez que el usuario escribe una letra.
//  Si no actualizamos el estado en onValorCambia, el campo NO cambia.
//  Esto se llama "estado elevado" (state hoisting).
// =====================================================================

/**
 * @param error      mensaje de error bajo el campo; null = sin error.
 * @param tipoTeclado Text, Email, Number, Phone... (cambia el teclado del celular)
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },                       // texto flotante (como <label>)
        leadingIcon = {                                   // ícono a la izquierda
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
        },
        isError = error != null,                          // borde rojo si hay error
        supportingText = { if (error != null) Text(error) }, // texto de ayuda bajo el campo
        singleLine = true,                                // una sola línea
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = ImeAction.Next                    // botón "Siguiente" en el teclado
        ),
        shape = EstiloCampo.forma,
        colors = EstiloCampo.colores()
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoTextoPreview() {
    MiRegistroTheme {
        CampoTexto(
            valor = "ana@",
            onValorCambia = {},
            etiqueta = "Correo electrónico",
            icono = R.drawable.ic_correo,
            error = "Ingresa un correo válido",
            modifier = Modifier.padding(16.dp)
        )
    }
}
