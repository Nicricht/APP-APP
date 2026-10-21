package cl.duocuc.segurosbpo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.style.estiloAltoBoton
import cl.duocuc.segurosbpo.ui.style.EstilosBotones
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme


@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    @DrawableRes icono: Int? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.estiloAltoBoton(),          // height: 52dp
        enabled = habilitado,
        shape = EstilosBotones.forma,                     // border-radius
        colors = EstilosBotones.colorPrincipal(),       // background / color
        elevation = EstilosBotones.elevacion(),           // box-shadow
        contentPadding = EstilosBotones.relleno           // padding
    ) {

        if (icono != null) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,              // decorativo: el texto ya describe
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        }
        Text(text = texto)
    }
}


@Preview(showBackground = true)
@Composable
private fun BotonPrincipalPreview() {
    SegurosBpoTheme {
        Column(Modifier.padding(16.dp)) {
            BotonPrincipal(texto = "Registrarme", onClick = {}, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.size(8.dp))
            BotonPrincipal(texto = "Con ícono", onClick = {}, icono = R.drawable.ic_check, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.size(8.dp))
            BotonPrincipal(texto = "Deshabilitado", onClick = {}, habilitado = false, modifier = Modifier.fillMaxWidth())
        }
    }
}
