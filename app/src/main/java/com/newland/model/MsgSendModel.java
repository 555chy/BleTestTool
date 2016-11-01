package com.newland.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.content.Context;

import com.newland.bletesttool.R;
import com.newland.global.Constant;
import com.newland.utils.HexConvertUtils;

/**
 * 发送的消息模型
 */
public class MsgSendModel {

	/** 消息的编码("UTF8"或"HEX") */
	public enum EncodingType {
		UTF8, HEX
	}

	/** 消息的编码 */
	protected EncodingType encodingType;
	/** 消息数据 */
	protected byte[] msg;

	protected MsgSendModel() {
	}

	public MsgSendModel(EncodingType encodingType, byte[] msg) {
		this.encodingType = encodingType;
		this.msg = msg;
	}

	public int getMsgLen() {
		if (msg == null) {
			return 0;
		}
		return msg.length;
	}

	public MsgSendModel(byte[] serializeByteArray, int offset, int len) throws IOException, IllegalArgumentException {
		ByteArrayInputStream bais = new ByteArrayInputStream(serializeByteArray, offset, len);
		DataInputStream dis = new DataInputStream(bais);
		byte ordinal = dis.readByte();
		if (ordinal < 0 || ordinal >= EncodingType.values().length) {
			throw new IllegalArgumentException();
		}
		encodingType = EncodingType.values()[ordinal];
		int msgLen = dis.readInt();
		if (msgLen < 0) {
			throw new IllegalArgumentException();
		} else if (msgLen == 0) {
			msg = null;
		} else {
			msg = new byte[msgLen];
			dis.read(msg);
		}
		dis.close();
		bais.close();
	}

	public EncodingType getEncodingType() {
		return encodingType;
	}

	/**
	 * 获取编码类型的字符串形式
	 */
	public String getEncodingTypeStr(Context context) {
		switch (encodingType) {
		case UTF8:
			return context.getResources().getString(R.string.utf8);
		case HEX:
			return context.getResources().getString(R.string.hex);
		}
		return null;
	}

	public byte[] getMsg() {
		return msg;
	}

	/**
	 * 将数据以16进制形式打印出来
	 */
	public String getMsgHexStr(Context context) {
		if (getMsgLen() == 0) {
			return "";
		}
		return HexConvertUtils.byteArrToHexStr(context, msg);
	}

	/**
	 * 将数据转化为字符串(在打印日志时使用)
	 */
	public String getMsgStr(Context context) {
		if (getMsgLen() > 0) {
			switch (encodingType) {
			case UTF8:
				try {
					return new String(msg, Constant.CHARSET_NAME);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			case HEX:
				return HexConvertUtils.byteArrToHexStr(context, msg);
			}
		}
		return "";
	}

	/**
	 * 将数据转化为字符串(在"发送历史记录"中使用)
	 */
	public String toString(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(encodingType);
		sb.append("] ");
		sb.append(getMsgStr(context));
		if (encodingType == EncodingType.UTF8) {
			sb.append(" (");
			sb.append(getMsgHexStr(context));
			sb.append(")");
		}
		return sb.toString();
	}

	/**
	 * 获取序列化后的字节数组
	 */
	public byte[] toSerializeByteArray() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		byte ordinal = (byte) encodingType.ordinal();
		dos.writeByte(ordinal);
		if (getMsgLen() <= 0) {
			dos.writeInt(0);
		} else {
			dos.writeInt(msg.length);
			dos.write(msg);
		}
		dos.close();
		baos.close();
		return baos.toByteArray();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MsgSendModel) {
			MsgSendModel model = (MsgSendModel) obj;
			if (encodingType != model.encodingType) {
				return false;
			}
			if (getMsgLen() != model.getMsgLen()) {
				return false;
			}
			for (int i = 0; i < getMsgLen(); i++) {
				if (msg[i] != model.getMsg()[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
