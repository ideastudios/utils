package tech.oom.julian.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by zzk on 15/11/28.
 */
public class BitmapUtil {

    /**
     *  图片模糊效果
     */
    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius){
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript rs = RenderScript.create(context);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, bitmap);

        blurScript.setRadius(radius);

        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        allOut.copyTo(outBitmap);

        bitmap.recycle();
        rs.destroy();

        return outBitmap;
    }

    /**
     *  图片黑白效果
     */
    public static Bitmap matrixBitmap(Context context, int drawableId){
        Drawable drawable = context.getResources().getDrawable(R.mipmap.avator);
        Bitmap srcBitmap = BitmapUtil.drawableToBitmap(drawable);
        float[] src = new float[]{
                0.28F, 0.60F, 0.40F, 0, 0,
                0.28F, 0.60F, 0.40F, 0, 0,
                0.28F, 0.60F, 0.40F, 0, 0,
                0, 0, 0, 1, 0,
        };
        ColorMatrix cm = new ColorMatrix(src);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        Bitmap resultBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap );
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setAlpha(100);
        paint.setColorFilter(f);
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return resultBitmap;
    }

    /**
     *  drawable转bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        return bitmap;
    }

    /**
     *
     * scale bitmap
     *
     * @param path file path
     * @param mContentResolver
     * @param maxSize max size for scale
     * @return bitmap
     */
    public static Bitmap scaleBitmap(String path, ContentResolver mContentResolver, int maxSize) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = maxSize;
            in = mContentResolver.openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }

            Bitmap b = null;
            in = mContentResolver.openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            return b;
        } catch (IOException e) {
            android.util.Log.e("LOG", e.getMessage(), e);
            return null;
        }

    }

    /**
     * save bitmap to given file
     * @param bitmap bitmap to save
     * @param path path of file
     * @return is success
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, String path) {

        File file = new File(path);
        FileOutputStream fOut;

        try {

            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;

    }

    public static int calculateInSampleSize1(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }


    public static int calculateInSampleSize2(
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

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize2(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 根据相关坐标 给bitmap画矩形框
     * @param bitmap 原始bitmap
     * @param left 左
     * @param top 上
     * @param right 右
     * @param bottom 下
     * @return Bitmap 画框之后的bitmap
     */
    public static Bitmap drawRectangleOnBitmap(Bitmap bitmap,float left,float top,float right,float bottom){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        right = Math.min(width-5, right);
        bottom = Math.min(height-5, bottom);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#F05A23"));
        paint.setStyle(Paint.Style.STROKE);//不填充
        paint.setStrokeWidth(5);
        canvas.drawRect(left, top, right, bottom, paint);
        return  bitmap;
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param degrees  旋转角度，可正可负
     * @return Bitmap 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float degrees) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /**
     * 按照要求的宽和高等比缩小bitmap
     * @param toTransform
     * @param maxItemWidth
     * @param maxItemHeight
     * @return Bitmap 缩小后的Bitmap
     */
    public static Bitmap transform(Bitmap toTransform, int maxItemWidth, int maxItemHeight) {
        int height = toTransform.getHeight();
        int width = toTransform.getWidth();
        float xScale = (maxItemWidth) / width;
        float yScale = (maxItemHeight) / height;
        float scale = Math.min(xScale, yScale);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap newBitmap = Bitmap.createBitmap(toTransform, 0, 0, width, height, matrix, true);
        LogUtils.d("source width "+width + " height "+ height);
        LogUtils.d("source width "+newBitmap.getWidth() + " height "+ newBitmap.getHeight());
//        System.out.println("source width "+width + " height "+ height );
//        System.out.println("source width "+newBitmap.getWidth() + " height "+ newBitmap.getHeight() );
        return newBitmap;
    }

}
