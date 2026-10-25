package cl.duocuc.segurosbpo.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duocuc.segurosbpo.R
import cl.duocuc.segurosbpo.ui.theme.Dimens

@Composable
fun AvatarCuenta(
    foto: ByteArray?,
    descripcion: String,
    modifier: Modifier = Modifier,
    tamano: Dp = Dimens.tamanoAvatar
) {
    val imagen = remember(foto) {
        foto?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    if (imagen == null) {
        AvatarCircular(
            imagen = R.drawable.avatar_usuario,
            descripcion = descripcion,
            modifier = modifier,
            tamano = tamano
        )
    } else {
        AvatarImagen(
            imagen = imagen,
            descripcion = descripcion,
            modifier = modifier,
            tamano = tamano
        )
    }
}

@Composable
fun AvatarImagen(
    imagen: ImageBitmap,
    descripcion: String,
    modifier: Modifier = Modifier,
    tamano: Dp = Dimens.tamanoAvatar
) {
    Image(
        bitmap = imagen,
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .border(Dimens.bordeAvatar, MaterialTheme.colorScheme.surface, CircleShape)
    )
}
