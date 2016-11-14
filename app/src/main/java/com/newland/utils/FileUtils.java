package com.newland.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    /**
     * 读取Assets目录下的文本文件的内容
     */
    public static String getAssetsFileText(Context context, String fileName) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        boolean isFirstLine = true;
        while ((line = br.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
            } else {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
