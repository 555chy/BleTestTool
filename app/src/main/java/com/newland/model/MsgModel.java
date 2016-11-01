package com.newland.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

import com.newland.bletesttool.R;

/**
 * 发送、接收、或打印日志的数据模型
 */
public class MsgModel extends MsgSendModel {

	/** 消息发送方向("发送"或"接收"或"打印日志") */
	public enum Direction {
		SEND, RECEIVED, LOG
	}

	/** 日志等级 */
	public enum LogLevel {
		NORMAL, ERROR
	}

	/** 时间戳 */
	private long timestamp;
	/** 消息发送方向 */
	private Direction direction;
	/** 日志 */
	private String log;

	public MsgModel(Direction direction, EncodingType encodingType, byte[] msg) {
		super(encodingType, msg);
		init(direction);
	}

	public MsgModel(MsgSendModel model) {
		super(model.getEncodingType(), model.getMsg());
		init(Direction.SEND);
	}
	
	public MsgModel(MsgSendModel model, Direction direction) {
		super(model.getEncodingType(), model.getMsg());
		init(direction);
	}

	/**
	 * 初始化日志数据模型
	 */
	public MsgModel(String log) {
		// 当消息为日志时，父类的对象不用
		init(Direction.LOG);
		this.log = log;
	}
	
	private void init(Direction direction) {
		this.direction = direction;
		this.timestamp = System.currentTimeMillis();
	}
	
	public String getTime() {
		Date date = new Date(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		return sdf.format(date);
	}

	public String getDirection(Context context) {
		switch (direction) {
		case SEND:
			return context.getResources().getString(R.string.direction_send);
		case RECEIVED:
			return context.getResources().getString(R.string.direction_received);
		case LOG:
			return context.getResources().getString(R.string.direction_log);
		}
		return null;
	}

	public String toString(Context context, boolean isShowTime) {
		StringBuffer sb = new StringBuffer();
		if (isShowTime) {
			sb.append(getTime() + " ");
		}
		if (direction == Direction.LOG) {
			sb.append(log);
		} else {
			sb.append(getDirection(context) + " ");
//			sb.append(getEncodingTypeStr(context) + " ");
//			sb.append(getMsgStr(context));
			sb.append(toString(context));
		}
		return sb.toString();
	}
}
