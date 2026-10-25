package cl.duocuc.segurosbpo.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.AvisoSuperior(estado: SnackbarHostState) {
    SnackbarHost(
        hostState = estado,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}
