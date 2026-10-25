package cl.duocuc.segurosbpo

import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException

/** Simula un proveedor de galería que falla sin una explicación utilizable. */
class ProveedorErrorFotoSinMensaje : DocumentoProveedorCobertura() {
    override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? =
        throw FileNotFoundException()
}
