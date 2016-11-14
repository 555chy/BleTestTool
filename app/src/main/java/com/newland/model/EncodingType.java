package com.newland.model;

import android.content.Context;

import com.newland.bletesttool.R;

/**
 * 消息的编码("UTF8"或"HEX")
 */
public enum EncodingType {
    HEX,
    UTF8;

    /**
     * 获取编码类型的字符串形式
     */
    public String getEncodingTypeStr(Context context) {
        int strId = 0;
        if (ordinal() == HEX.ordinal()) {
            strId = R.string.hex;
        } else if (ordinal() == UTF8.ordinal()) {
            strId = R.string.utf8;
        }
        return strId == 0 ? "" : context.getResources().getString(strId);
    }
}
