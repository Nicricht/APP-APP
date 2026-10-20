package cl.duocuc.miregistro.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.R
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

// =====================================================================
//  AvatarCircular.kt  —  Imagen recortada en círculo con borde.
//  ---------------------------------------------------------------------
//  En CSS:  img { width:120px; height:120px; border-radius:50%;
//                 border:4px solid white; object-fit:cover; }
//  ¡El ORDEN de los modificadores importa! (se aplican de arriba a abajo)
//     size   -> tamaño
//     shadow -> sombra circular
//     clip   -> recorta en círculo
//     border -> dibuja el borde encima del recorte
// =====================================================================

@Composable
fun AvatarCircular(
    @DrawableRes imagen: Int,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = imagen),
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(Dimens.tamanoAvatar)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview() {
    MiRegistroTheme {
        AvatarCircular(imagen = R.drawable.avatar_usuario, descripcion = "", modifier = Modifier.padding(16.dp))
    }
}
