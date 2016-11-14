package com.newland.utils;

import android.widget.Spinner;

public class BusinessLogicalUtils {

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
