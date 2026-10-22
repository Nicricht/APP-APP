package cl.duocuc.segurosbpo.ui.components

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.theme.Dimens
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme


@Composable
fun AvatarCircular(
    @DrawableRes imagen: Int,
    descripcion: String,
    modifier: Modifier = Modifier,
    tamano: Dp = Dimens.tamanoAvatar
) {
    Image(
        painter = painterResource(id = imagen),
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
private fun AvatarCircularPreview() {
    SegurosBpoTheme {
        AvatarCircular(imagen = R.drawable.avatar_usuario, descripcion = "", modifier = Modifier.padding(16.dp))
    }
}
