package com.abdulfatir.concanalyzer.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Abdul on 3/12/2016.
 */
public class ImageUtils {

    /**
     * Returns a less resolution Bitmap with specified width maintaining aspect ratio.
     *
     * @param filePath    the file path
     * @param widthNeeded the width needed
     * @return the bitmap
     */
    public static Bitmap lessResolution(String filePath, int widthNeeded) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        int imageWidth = opts.outWidth;
        int imageHeight = opts.outHeight;

        double aspectRatio = ((double) imageWidth) / imageHeight;
        int heightNeeded = (int) (widthNeeded / aspectRatio);

        opts.inSampleSize = calculateInSampleSize(opts, widthNeeded, heightNeeded);
        opts.inJustDecodeBounds = false;

        Bitmap orig = rotateBitmap(BitmapFactory.decodeFile(filePath, opts), getOrientation(filePath));

        Bitmap scaled = Bitmap.createScaledBitmap(orig, widthNeeded, heightNeeded, true);
        if (!scaled.equals(orig)) {
            orig.recycle();
            orig = null;
        }
        System.gc();
        return scaled;
    }


    /**
     * Rotate the image according to EXIF data.
     *
     * @param bitmap      the bitmap
     * @param orientation the orientation
     * @return the rotated bitmap
     */
    @Nullable
    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        matrix.reset();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            Log.d("debug", "done!");
            bitmap.recycle();
            bitmap = null;
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the EXIF orientation of the image specified by path
     * @param path Path of the image
     * @return orientation
     */

    private static int getOrientation(String path) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        return orient;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
