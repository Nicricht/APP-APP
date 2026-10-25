package cl.duocuc.segurosbpo

import android.net.Uri

/** Simula un proveedor de galería que falla sin una explicación utilizable. */
class ProveedorErrorFotoSinMensaje : DocumentoProveedorCobertura() {
    override fun getType(uri: Uri): String? = throw IllegalStateException()
}
