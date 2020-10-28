package io.anyline.examples.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;

/**
 *
 * BitmapUtil to load bitmaps efficiently
 **/
public class BitmapUtil {

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
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeResource(res, resId, options);
        return bm;
        //return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap getBitmap (String path){
        Bitmap scaleImage = null;
        try {
            FileInputStream fi = new FileInputStream(path);
            Bitmap bitmapImage = BitmapFactory.decodeStream(fi);
            scaleImage = Bitmap.createScaledBitmap(bitmapImage, bitmapImage.getWidth() * 2  , bitmapImage.getHeight() * 2, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return scaleImage;

    }

    public static Bitmap getBitmapLow (String path){
        Bitmap scaleImage = null;
        try {
            FileInputStream fi = new FileInputStream(path);
            Bitmap bitmapImage = BitmapFactory.decodeStream(fi);
            scaleImage = Bitmap.createScaledBitmap(bitmapImage, bitmapImage.getWidth()  , bitmapImage.getHeight(), false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return scaleImage;

    }

}
