package com.newland.ble.callback;

/**
 * 界面提供给SDK接口的回调函数
 */
public interface IBleCallback {

	/** 是否正常获取到gattService */
	void onGetGattService(boolean result);

	/** 是否正常获取到gattReadCharacteristic */
	void onGetReadCharacteristic(boolean result);

	/** 是否正常获取到gattWriteCharacteristic */
	void onGetWriteCharacteristic(boolean result);

	/** 断开连接("主动断开"或"被动断开") */
	void onDisconnect(boolean isManual);

	/** 接收到数据 */
	void onRead(byte[] value);

	/**
	 * 数据发送结果回调
	 *
	 * @param result                    发送是否成功
	 * @param errMsg                    发送失败时的错误描述
	 * @param preparedSendBytesCount 预计要发送的字节总数
	 * @param sendSuccBytesCount      发送成功的字节数
	 */
	void onWriteReturn(boolean result, String errMsg, int preparedSendBytesCount, int sendSuccBytesCount);

	/** 开始重连 */
	void onReconnectStart();

	/** 重连中 */
	void onReconnecting();

	/** 重连结束 */
	void onReconnectEnd();

	/** 重连结果("参数错误"或"重连成功") */
	void onReconnectStateChange(boolean isConnected, String errMsg);

	/** 打印日志 */
	void log(String log);
}
