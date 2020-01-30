package it.uniupo.museumguide.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PhotoUtil {

    // Il seguente metodo permette di ridimensiorare un'immagine
    public static Bitmap setPic(Context context, Uri uri) throws IOException {
        int targetW = 600;
        int targetH = 600;
        // Ottiene le dimensioni della bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determina di quanto ridimensionare l'immagine
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        String photoPath = getRealPathFromURI(context, uri);
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

        bitmap = rotateImageIfRequired(photoPath, bitmap);

        return bitmap;
    }

    // Metodo che permete di ruotare l'immagine se e' necessario
    private static Bitmap rotateImageIfRequired(String photoPath, Bitmap bitmap) throws IOException {
        ExifInterface exifInterface = new ExifInterface(photoPath);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Metodo che permette di trovare il percorso reale della foto
    private static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] column = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, column, null, null, null);
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Metodo che permette la conversione di un'immagine da formato BLOB a Bitmap
    public static Bitmap getBitmapFromBytes(byte[] bytes) {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    // Metodo che permette la conversione di un'immagine da formato Bitmap a BLOB
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);
            return out.toByteArray();
        }
        return null;
    }

    public static void updateImageView(Context context, Uri uri, ImageView imageView, final ProgressBar progressBar) {
        if (!((Activity) context).isFinishing()) {
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, java.lang.Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, java.lang.Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(imageView);
        }
    }
}
