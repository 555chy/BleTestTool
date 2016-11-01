package com.newland.ble.callback;

import android.bluetooth.BluetoothGatt;

public interface IConnectionChangeCallback {
	/** 连接过程完成(可能是连接"成功"或"失败") */
	public void onConnectFinish(boolean connectResult, boolean discoveredResult, BluetoothGatt gatt);

	/** 连接断开(只要尝试连接,无论是否曾连接上都会触发该事件) */
	public void onDisconnect();

}
