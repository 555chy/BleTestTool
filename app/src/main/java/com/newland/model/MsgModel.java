package com.newland.model;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class MsgModel {

    /** 记录时间 */
    protected String time;

    protected MsgModel() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        time = sdf.format(date);
    }

    public String getTime() {
        return time;
    }

    public abstract String toString(Context context, boolean isShowTime, boolean isShowColor);
}
