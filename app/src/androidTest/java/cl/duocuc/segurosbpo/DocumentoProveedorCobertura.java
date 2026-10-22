package cl.duocuc.segurosbpo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Base64;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DocumentoProveedorCobertura extends ContentProvider {
    @Override public boolean onCreate() {
        return true;
    }

    @Override public String getType(Uri uri) {
        return uri.getQueryParameter("mime");
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME});
        cursor.addRow(new Object[]{uri.getQueryParameter("nombre")});
        return cursor;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File directorio = getContext().getFilesDir();
        if (!directorio.exists() && !directorio.mkdirs()) {
            throw new FileNotFoundException("No se puede crear el directorio de prueba");
        }
        File archivo = new File(directorio, "qa_contenido.bin");
        byte[] contenido;
        if ("1".equals(uri.getQueryParameter("grande"))) {
            contenido = new byte[5 * 1024 * 1024 + 1];
            byte[] firma = "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            System.arraycopy(firma, 0, contenido, 0, firma.length);
        } else {
            String datos = uri.getQueryParameter("contenido");
            contenido = Base64.decode(datos == null ? "" : datos, Base64.URL_SAFE | Base64.NO_WRAP);
        }
        try (FileOutputStream flujo = new FileOutputStream(archivo)) {
            flujo.write(contenido);
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
        return ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
