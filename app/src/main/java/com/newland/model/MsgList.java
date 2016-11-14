package com.newland.model;

import android.content.Context;

import java.util.ArrayList;

/**
 * 发送、接收的消息或日志列表
 */
public class MsgList {

    /** 记录的最大消息数 */
    public static final int MAX_MSG_LEN = 200;

    private ArrayList<MsgModel> msgList;

    public MsgList() {
        msgList = new ArrayList<MsgModel>();
    }

    /**
     * 添加消息，但消息列表项不应超过{@link #MAX_MSG_LEN }
     */
    public void add(MsgModel model) {
        synchronized (this) {
            msgList.add(0, model);
            while (msgList.size() > MAX_MSG_LEN) {
                msgList.remove(msgList.size() - 1);
            }
        }
    }

    public void clear() {
        synchronized (this) {
            msgList.clear();
        }
    }

    public String toString(Context context, boolean isShowTime, boolean isShowColor) {
        StringBuilder sb = new StringBuilder();
        String str;
        synchronized (this) {
            for (int i = 0; i < msgList.size(); i++) {
                MsgModel model = msgList.get(i);
                str = model.toString(context, isShowTime, isShowColor);
                sb.append(str);
                if(isShowColor) {
                    sb.append("<br/>");
                } else {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
