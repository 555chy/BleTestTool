package com.newland.model;

import android.content.Context;

public class MsgLogModel extends MsgModel{

    /** 日志等级 */
    public enum LogLevel {
        NORMAL, ERROR
    }

    /** 日志等级 */
    private LogLevel logLevel;
    /** 日志 */
    private String log;

    public MsgLogModel(LogLevel logLevel, String log) {
        super();
        this.logLevel = logLevel;
        this.log = log;
    }

    @Override
    public String toString(Context context, boolean isShowTime, boolean isShowColor) {
        StringBuilder sb = new StringBuilder();
        if(isShowColor) {
            if(logLevel == LogLevel.ERROR) {
                sb.append("<font color=\"red\">");
            }
        }
        if (isShowTime) {
            sb.append(getTime());
            sb.append(" ");
        }
        sb.append(log);
        if(isShowColor) {
            if(logLevel == LogLevel.ERROR) {
                sb.append("</font>");
            }
        }
        return sb.toString();
    }
}
