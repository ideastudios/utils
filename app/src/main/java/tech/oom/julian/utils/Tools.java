package tech.oom.julian.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Patterns;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ubuntu_ivo on 17.07.15..
 */
public class Tools {

    /**
     * checked if android version is above given version
     * @param version version to compare
     * @return true is above, false is equals or below
     */
    public static boolean isBuildOver(int version) {
        if (android.os.Build.VERSION.SDK_INT > version)
            return true;
        return false;
    }

    /**
     * generate random string with given length
     * @param length length for string to generate
     * @return generated string
     */
    public static String generateRandomString(int length) {
        String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    /**
     * generate date for given format
     * @param format format for date
     * @param timestamp date in milliseconds to generate
     * @return string
     */
    public static String generateDate(String format, long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format(format, cal).toString();
        return date;
    }



    /**
     * copy stream without progress listener
     * @param is input stream
     * @param os output stream
     */
    public static void copyStream(InputStream is, OutputStream os) {
        copyStream(is, os, -1/*, null*/);
    }

    /**
     *
     * copy stream with progress listener
     *
     * @param is input stream
     * @param os output stream
     * @param length length of stream
     */
    public static void copyStream(InputStream is, OutputStream os, long length /*DownloadFileManager.OnDownloadListener listener*/) {
        final int buffer_size = 1024;
        int totalLen = 0;
        try {

            byte[] bytes = new byte[buffer_size];
            while (true) {
                // Read byte from input stream

                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
//                    listener.onFinishDownload();
                    break;
                }

                // Write byte from output stream
                if (length != -1/* && listener != null*/) {
                    totalLen = totalLen + count;
//                    listener.onProgress(totalLen);
                }
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    /**
     * return string of given size example 1MB, 1,4GB, 300KB
     * @param size size to convert
     * @return string
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }



    /**
     * get mime type of given file
     * @param url file path or url
     * @return
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }







    /**
     * save string to file
     * @param dataInput string to save
     * @param path path of file
     * @return
     */
    public static boolean saveStringToFile(String dataInput, String path) {
        String filename = path;

        File file = new File(filename);
        FileOutputStream fos;

        byte[] data = dataInput.getBytes();
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    public static boolean checkGrantResults (int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * copy text to clipboard and show toast info
     * @param text string to copy
     * @return
     */
    public static void copyTextFromTextViewAndShowToast(String text, Context c){
        ClipData clipData = ClipData.newPlainText("copy", text);
        ClipboardManager clipboard = (ClipboardManager) c.getSystemService(c.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(clipData);

        Toast.makeText(c, "Message copied to clipboard", Toast.LENGTH_LONG).show();
    }

    /**
     * get uri from imageView to share image
     * @param imageView imageView contains image to get uri
     * @return
     */
    public static Uri getLocalBitmapUri(ImageView imageView, Context context) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getTempFile(context, "temp") + "/temp.png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }



    /**
     * check if message has link
     * @param msg message
     * @return result not null if message has string
     */
    public static String checkForLink(String msg) {

        if (msg == null) {
            return null;
        } else if(msg.contains("@")){
            return null;
        } else {

            String result = null;
            Pattern pattern = Patterns.WEB_URL;
            Matcher matchURL = pattern.matcher(msg);

            if (matchURL.find()) {
                result = matchURL.group();
            }

            return result;
        }

    }

}
