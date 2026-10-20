package cl.duocuc.miregistro.ui.style

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.GrisDeshabilitado

object EstiloCampo {
    val forma = RoundedCornerShape(Dimens.radioCampo)

    @Composable
    fun colores(): TextFieldColors= OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = GrisDeshabilitado,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        cursorColor = MaterialTheme.colorScheme.primary
    )
}