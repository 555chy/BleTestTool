package com.newland.ble.callback;

import android.bluetooth.BluetoothDevice;

/**
 * Ble扫描回调<br>
 *
 * @author chy
 */
public interface IBleScanCallback {

	/** 蓝牙未打开 */
	void onBluetoothNotOpen();

	/** 开始扫描 */
	void onScanStarted();

	/** 每扫描到一个设备调用一次该函数 */
	void onScanResult(BluetoothDevice paramBluetoothDevice, int rssi);

	/** "扫描时间到"或者"手动停止扫描" */
	void onScanFinished();

	/** 扫描出错(仅当API>21时有意义),自动重启蓝牙 */
	void onScanFail(int errorCode);
}
