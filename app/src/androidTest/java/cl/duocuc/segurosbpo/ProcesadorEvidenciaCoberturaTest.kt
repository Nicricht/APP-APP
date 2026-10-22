package cl.duocuc.segurosbpo

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

class DocumentoProveedorCobertura : ContentProvider() {
    companion object {
        var contenido: ByteArray = byteArrayOf()
        var formato = "application/pdf"
        var nombre: String? = "archivo.pdf"
    }

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = formato

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME)).apply {
        addRow(arrayOf(nombre))
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val archivo = File(requireNotNull(context).cacheDir, "qa_evidencia.bin")
        archivo.writeBytes(contenido)
        return ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

@RunWith(AndroidJUnit4::class)
class ProcesadorEvidenciaCoberturaTest {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val uri = Uri.parse("content://cl.duocuc.segurosbpo.qa.archivos/documento")

    private fun procesar(mime: String, bytes: ByteArray, nombre: String? = "archivo"): Result<cl.duocuc.segurosbpo.data.ArchivoEvidencia> {
        DocumentoProveedorCobertura.formato = mime
        DocumentoProveedorCobertura.contenido = bytes
        DocumentoProveedorCobertura.nombre = nombre
        return runCatching { ProcesadorEvidencia.desdeSelector(context, uri) }
    }

    @Test fun aceptaPdfYCompruebaNombreDelDocumento() {
        val archivo = procesar("application/pdf", "%PDF-1.5".toByteArray()).getOrThrow()
        assertEquals("application/pdf", archivo.mime)
        assertEquals("archivo", archivo.nombre)
        assertEquals("%PDF-1.5", String(archivo.bytes))
        assertEquals("documento", procesar("application/pdf", "%PDF-1.4".toByteArray(), null).getOrThrow().nombre)
    }

    @Test fun validaFirmasDeJpgPngYWebp() {
        val jpg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte())
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0, 0, 0, 0)
        val webp = "RIFF1234WEBP".toByteArray()
        assertTrue(procesar("IMAGE/JPEG", jpg).isSuccess)
        assertTrue(procesar("image/png", png).isSuccess)
        assertTrue(procesar("image/webp", webp).isSuccess)
        assertTrue(procesar("image/jpeg", byteArrayOf(1, 2, 3)).isFailure)
        assertTrue(procesar("image/png", byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)).isFailure)
        assertTrue(procesar("image/webp", "RIFF1234NADA".toByteArray()).isFailure)
    }

    @Test fun rechazaFormatosInvalidosYArchivosDemasiadoGrandes() {
        assertTrue(procesar("text/plain", "texto".toByteArray()).isFailure)
        assertTrue(procesar("application/pdf", byteArrayOf()).isFailure)
        assertTrue(procesar("application/pdf", "Incorrecto".toByteArray()).isFailure)
        val enorme = ByteArray(ValidadorEvidencia.MAX_BYTES + 1)
        "%PDF-".toByteArray().copyInto(enorme)
        assertTrue(procesar("application/pdf", enorme).isFailure)
    }

    @Test fun camaraGeneraJpgComprimido() {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        try {
            val resultado = ProcesadorEvidencia.desdeCamara(bitmap)
            assertEquals("foto_camara.jpg", resultado.nombre)
            assertEquals("image/jpeg", resultado.mime)
            assertTrue(resultado.bytes.isNotEmpty())
        } finally {
            bitmap.recycle()
        }
    }
}
