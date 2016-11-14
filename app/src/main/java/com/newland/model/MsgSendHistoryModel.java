package com.newland.model;

import android.content.Context;

import com.newland.global.Constants;
import com.newland.utils.HexConvertUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 发送历史记录
 */
public class MsgSendHistoryModel {

	/** 消息的编码 */
	protected EncodingType encodingType;
	/** 消息数据 */
	protected byte[] msg;

	public MsgSendHistoryModel(EncodingType encodingType, byte[] msg) {
		this.encodingType = encodingType;
		this.msg = msg;
	}

	/**
	 * 从序列化后的字节数组中加载数据
	 */
	public MsgSendHistoryModel(byte[] serializeByteArray, int offset, int len) throws IOException, IllegalArgumentException {
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

	/**
	 * 获取编码类型
	 */
	public EncodingType getEncodingType() {
		return encodingType;
	}

	/**
	 * 获取消息字节数组形式的长度
	 */
	public int getMsgLen() {
		if (msg == null) {
			return 0;
		}
		return msg.length;
	}

	public byte[] getMsg() {
		return msg;
	}

	/**
	 * 将数据转化为字符串(在打印日志时使用)
	 */
	public String getMsgStr(Context context) {
		String str = "";
		if (getMsgLen() > 0) {
			switch (encodingType) {
				case HEX:
					str = HexConvertUtils.byteArrToHexStr(context, msg);
					break;
				case UTF8:
					try {
						str = new String(msg, Constants.CHARSET_NAME);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
			}
		}
		return str;
	}

	/**
	 * 将数据转化为字符串(在"发送历史记录"中使用)
	 */
	public String toString(Context context) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(encodingType);
		sb.append("] ");
		sb.append(getMsgStr(context));
		if (encodingType == EncodingType.UTF8) {
			sb.append(" (");
			sb.append(HexConvertUtils.byteArrToHexStrBuff(context, msg));
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
		if (obj instanceof MsgSendHistoryModel) {
			MsgSendHistoryModel model = (MsgSendHistoryModel) obj;
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
