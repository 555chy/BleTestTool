package com.newland.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Spinner;

public class MyUtils {

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

    /**
     * 获取下列框指定位置的整数值
     */
    public static int getSpinnerIntValue(Spinner spanner, int position) {
        if (position > 0 && position < spanner.getAdapter().getCount()) {
            try {
                return Integer.parseInt(String.valueOf(spanner.getItemAtPosition(position)));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 正确的uuid格式
     * 00000000-0000-0000-0000-000000000000
     *
     * @return uuid字符串是否合法
     */
    public static boolean verifyUuid(String uuid) {
        if (uuid.length() == 36) {
            return true;
        }
        return false;
    }
}
