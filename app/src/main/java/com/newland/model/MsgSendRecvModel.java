package com.newland.model;

import android.content.Context;

import com.newland.bletesttool.R;
import com.newland.utils.HexConvertUtils;

import java.nio.charset.Charset;

/**
 * 发送、接收、或打印日志的数据模型
 */
public class MsgSendRecvModel extends MsgModel {

    /** 消息发送方向("发送"或"接收") */
    public enum Direction {
        RECEIVED, SEND
    }

    /** 消息发送方向 */
    private Direction direction;
    /** 消息编码类型 */
    private EncodingType encodingType;
    /** 将接收或发送的消息解析成字符串形式 */
    private String msg = "";

    public MsgSendRecvModel(Context context, Direction direction, EncodingType encodingType, byte[] msg) {
        super();
        init(context, direction, encodingType, msg);
    }

    public MsgSendRecvModel(Context context, MsgSendHistoryModel model) {
        super();
        init(context, Direction.SEND, model.getEncodingType(), model.getMsg());
    }

    private void init(Context context, Direction direction, EncodingType encodingType, byte[] msg) {
        this.direction = direction;
        this.encodingType = encodingType;
        if (msg != null && msg.length > 0) {
            switch (encodingType) {
                case HEX:
                    this.msg = HexConvertUtils.byteArrToHexStr(context, msg);
                    break;
                case UTF8:
                    StringBuilder sb = new StringBuilder();
                    sb.append(new String(msg, Charset.forName("utf8")));
                    sb.append(" (");
                    sb.append(HexConvertUtils.byteArrToHexStrBuff(context, msg));
                    sb.append(")");
                    this.msg = sb.toString();
                    break;
            }
        }
    }

    /**
     * 获取消息发送方向的字符串形式
     */
    public String getDirectionStr(Context context) {
        int strId = 0;
        switch (direction) {
            case RECEIVED:
                strId = R.string.direction_received;
                break;
            case SEND:
                strId = R.string.direction_send;
                break;
        }
        return strId == 0 ? "" : context.getResources().getString(strId);
    }

    /**
     * 获取编码类型的字符串形式
     */
    public String getEncodingTypeStr(Context context) {
        int strId = 0;
        switch (encodingType) {
            case HEX:
                strId = R.string.hex;
                break;
            case UTF8:
                strId = R.string.utf8;
                break;
        }
        return strId == 0 ? "" : context.getResources().getString(strId);
    }

    public String toString(Context context, boolean isShowTime, boolean isShowColor) {
        StringBuilder sb = new StringBuilder();
        if(isShowColor) {
            if(direction == Direction.RECEIVED) {
                sb.append("<font color=\"green\">");
            } else {
                sb.append("<font color=\"blue\">");
            }
        }
        if (isShowTime) {
            sb.append(getTime());
            sb.append(" ");
        }
        sb.append(getDirectionStr(context));
        sb.append(" ");
        sb.append(getEncodingTypeStr(context));
        sb.append(" ");
        sb.append(msg);
        if(isShowColor) {
            sb.append("</font>");
        }
        return sb.toString();
    }
}
