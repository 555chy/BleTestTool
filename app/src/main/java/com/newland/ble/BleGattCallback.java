package com.newland.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.newland.ble.callback.IExecResultCallback;
import com.newland.ble.callback.IConnectionChangeCallback;
import com.newland.utils.HexConvertUtils;

/**
 * Ble连接、读写回调函数
 */
public class BleGattCallback extends BluetoothGattCallback {

	private static final String TAG = "BleGattCallback";

	private Context context;
	private IConnectionChangeCallback connectStateChangeCallback;
	private IExecResultCallback execResultCallback;

	public BleGattCallback(Context context, IConnectionChangeCallback callback) {
		this.context = context;
		this.connectStateChangeCallback = callback;
	}

	public void setExecResultCallback(IExecResultCallback execResultCallback) {
		this.execResultCallback = execResultCallback;
	}

	/**
	 * 连接状态改变
	 */
	@Override
	public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
		Log.i(TAG, "onConnectionStateChange, newState=" + newState);
		if (newState == BluetoothGatt.STATE_CONNECTED) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 连接上了以后就开始搜索服务
					gatt.discoverServices();
				}
			}).start();
		} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
			connectStateChangeCallback.onDisconnect();
		}
	}

	/**
	 * 搜索服务结果<br>
	 * 只有连接状态为{@link BluetoothGatt#STATE_CONNECTED},并且搜索服务结果为
	 * {@link BluetoothGatt#GATT_SUCCESS},同时相应的服务和特征被找到，才算连接成功
	 */
	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		Log.i(TAG, "onServicesDiscovered, status=" + status);
		connectStateChangeCallback.onConnectFinish(true, status == BluetoothGatt.GATT_SUCCESS, gatt);
	}

	/**
	 * 服务器向客户端发送数据
	 */
	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		Log.i(TAG, "onCharacteristicChanged, len=" + characteristic.getValue().length + ", value=" + HexConvertUtils.byteArrToHexStr(context, characteristic.getValue()));
		if (execResultCallback != null) {
			execResultCallback.onNotifyReturn(characteristic.getUuid(), characteristic.getValue());
		}
	}

	/**
	 * 客户端向服务器端发送数据的结果
	 */
	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		Log.i(TAG, "onCharacteristicWrite, status=" + status + ", len=" + characteristic.getValue().length + ", value=" + HexConvertUtils.byteArrToHexStr(context, characteristic.getValue()));
		if (execResultCallback != null) {
			execResultCallback.onWriteReturn(characteristic.getUuid(), status == BluetoothGatt.GATT_SUCCESS);
		}
	}

}
