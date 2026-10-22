package cl.duocuc.segurosbpo;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Base64;
import java.io.FileNotFoundException;
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
        byte[] contenido;
        if ("1".equals(uri.getQueryParameter("grande"))) {
            contenido = new byte[5 * 1024 * 1024 + 1];
            byte[] firma = "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            System.arraycopy(firma, 0, contenido, 0, firma.length);
        } else {
            String datos = uri.getQueryParameter("contenido");
            contenido = Base64.decode(datos == null ? "" : datos, Base64.URL_SAFE | Base64.NO_WRAP);
        }
        try {
            ParcelFileDescriptor[] extremos = ParcelFileDescriptor.createPipe();
            Thread escritor = new Thread(() -> {
                try (ParcelFileDescriptor.AutoCloseOutputStream salida =
                    new ParcelFileDescriptor.AutoCloseOutputStream(extremos[1])) {
                    salida.write(contenido);
                } catch (IOException ignored) {
                }
            });
            escritor.start();
            return extremos[0];
        } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
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
