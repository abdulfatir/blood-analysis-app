package com.abdulfatir.concanalyzer.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Abdul on 3/12/2016.
 */
public class ImageUtils {

    public static Bitmap lessResolution(String filePath, int widthNeeded) {


        Bitmap orig = rotateBitmap(BitmapFactory.decodeFile(filePath), getOrientation(filePath));
        final int height = orig.getHeight();
        final int width = orig.getWidth();

        double aspectRatio = ((double) width) / height;
        int reqHeight = (int) (widthNeeded / aspectRatio);
        int reqWidth = widthNeeded;

        Bitmap scaled = Bitmap.createScaledBitmap(orig, reqWidth, reqHeight, true);
        if (!scaled.equals(orig)) {
            orig.recycle();
            orig = null;
        }

        System.gc();
        Log.d("size", width + "," + height + "," + reqWidth + "," + reqHeight);

        return scaled;
    }


    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }


    @Nullable
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

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
            Log.d("rotate", "rotate" + orientation + matrix.toShortString());
            //Matrix m = new Matrix();
            //m.reset();
            //m.postRotate(90);
            Log.d("sizeBefore", bitmap.getWidth() + "," + bitmap.getHeight());
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //Log.d("sizeAfter", bmRotated.getWidth()+","+bmRotated.getHeight());
            bitmap.recycle();
            bitmap = null;
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            Log.d("error", "OOM");
            return null;
        }
    }


    private static int getOrientation(String path) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Log.d("orient", orient + "");
        return orient;
    }

}
