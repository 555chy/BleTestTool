package com.newland.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

import com.newland.global.Constant;

/**
 * 发送历史记录
 */
public class SendHistoryModel {
    private ArrayList<MsgSendModel> msgSendList;

    public SendHistoryModel(String serializeStr) {
        msgSendList = new ArrayList<MsgSendModel>();
        if (serializeStr != null) {
            byte[] buffer = serializeStr.getBytes();
            byte count = buffer[0];
            if (count > 0) {
                int index = 1;
                try {
                    for (byte i = 0; i < count; i++) {
                        int len = buffer.length - index;
                        if (len <= 0) {
                            break;
                        }
                        MsgSendModel model = new MsgSendModel(buffer, index, len);
                        index = index + 1 + 4 + model.getMsgLen();
                        msgSendList.add(model);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msgSendList.clear();
                }
            }
        }
    }

    public int getSize() {
        return msgSendList.size();
    }

    public MsgSendModel get(int position) {
        if (msgSendList == null || position >= msgSendList.size()) {
            return null;
        }
        return msgSendList.get(position);
    }

    public ArrayList<MsgSendModel> getSendHistoryList() {
        return msgSendList;
    }

    /**
     * 添加新发送的对象(如果已经有了就不重复添加了)
     */
    public void addSendModel(MsgSendModel model) {
        int index = msgSendList.indexOf(model);
        if (index == -1) {
            if (msgSendList.size() > Constant.MAX_SEND_HISTORY_COUNT) {
                msgSendList.remove(msgSendList.size() - 1);
            }
        } else {
            msgSendList.remove(index);
        }
        msgSendList.add(0, model);
    }

    /**
     * 将"发送历史记录"转化为字符串数组
     */
    public String[] toStrArray(Context context) {
        if (msgSendList.size() == 0) {
            return null;
        }
        String[] array = new String[msgSendList.size()];
        for (int i = 0; i < msgSendList.size(); i++) {
            array[i] = msgSendList.get(i).toString(context);
        }
        return array;
    }

    /**
     * 将发送历史记录序列化为字符串
     */
    public String toSerializeStr() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte count = (byte) msgSendList.size();
        baos.write(new byte[]{count});
        if (count > 0) {
            for (byte i = 0; i < count; i++) {
                baos.write(msgSendList.get(i).toSerializeByteArray());
            }
        }
        baos.close();
        return baos.toString();
    }

}
