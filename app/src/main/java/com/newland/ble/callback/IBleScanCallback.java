package com.newland.ble.callback;

import com.newland.ble.BleScanFilter;

import android.bluetooth.BluetoothAdapter.LeScanCallback;

/**
 * Ble扫描回调<br>
 * 需要在{@link #onLeScan}中记录扫描到的设备,在{@link #onScanFinished}中弹出列表供用户选择连接的设备<br>
 * 或者在{@link #onLeScan}中发现所需的设备之后,调用{@link BleScanFilter#stopLeScan}停止搜索,然后连接相应设备
 */
public interface IBleScanCallback extends LeScanCallback {

	/**
	 * 蓝牙未打开
	 */
	void onBluetoothNotOpen();

	/**
	 * 当扫描时间到, 然后正常结束时, 会回调该方法
	 */
	void onScanStarted();

	/**
	 * "扫描时间到"或者"手动停止扫描"
	 */
	void onScanFinished();
}
