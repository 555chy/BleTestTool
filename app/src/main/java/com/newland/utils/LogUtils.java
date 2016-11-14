package com.newland.utils;

import android.content.Context;
import android.os.Environment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {

    public static String getStackTrace(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            sb.append(element.toString());
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public static void logToFile(Context context, final Throwable throwable) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
            String path = context.getExternalCacheDir().getAbsolutePath() + System.getProperty("file.separator") + sdf.format(new Date()) + ".txt";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                fos.write(getStackTrace(throwable).getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
