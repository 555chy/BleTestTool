package com.newland.utils;

import com.newland.bletesttool.R;

import android.content.Context;

/**
 * 字节数组与16进制字符串相互转换的工具
 */
public class HexConvertUtils {

    /**
     * 字节数组转化为16进制字符串
     */
    public static String byteArrToHexStr(Context context, byte[] buffer) throws IllegalArgumentException {
        StringBuffer sb = byteArrToHexStrBuff(context, buffer);
        return sb == null ? null : sb.toString();
    }

    /**
     * 字节数组转换为16进制字符串缓冲器
     */
    public static StringBuffer byteArrToHexStrBuff(Context context, byte[] buffer) throws IllegalArgumentException {
        if (buffer == null) {
            throw new IllegalArgumentException(context.getResources().getString(R.string.err_buff_cannot_be_null));
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buffer.length; i++) {
            if (i != 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02x", buffer[i]));
        }
        return sb;
    }

    /**
     * 将1个16进制字符转为1个字节
     */
    public static byte getByte(Context context, char c) throws IllegalArgumentException {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (c - 'a' + 10);
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else {
            throw new IllegalArgumentException(context.getString(R.string.err_str_has_illegal_char));
        }
    }

    /**
     * 16进制字符串转字节数组
     */
    public static byte[] hexStrToByteArr(Context context, String hexStr) throws IllegalArgumentException {
        int errStrId = 0;
        if (hexStr == null) {
            errStrId = R.string.err_str_cannot_be_null;
        } else if (hexStr.length() == 0) {
            errStrId = R.string.err_str_len_cannot_be_zero;
        } else if (hexStr.length() % 2 != 0) {
            errStrId = R.string.err_str_len_must_be_even;
        }
        if (errStrId != 0) {
            throw new IllegalArgumentException(context.getResources().getString(errStrId));
        }
        int index = 0;
        byte[] buffer = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length(); i += 2, index++) {
            byte high = getByte(context, hexStr.charAt(i));
            byte low = getByte(context, hexStr.charAt(i + 1));
            buffer[index] = (byte) (high << 4 | low);
        }
        return buffer;
    }
}
