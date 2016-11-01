package com.newland.ble.callback;

import java.util.UUID;

public interface IExecResultCallback {

	/** 收到服务器发送的数据时触发 */
	public void onNotifyReturn(UUID uuid, byte[] value);

	/** 获得向服务器发送数据成功与否的结果时触发 */
	public void onWriteReturn(UUID uuid, boolean result);
}
