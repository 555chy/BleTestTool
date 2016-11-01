package com.newland.ble;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.newland.global.Constant;

/**
 * Ble连接参数
 * 
 * <pre>
 * uuid共48位，格式如下
 * 00000000-0000-0000-8000-000000000000
 * </pre>
 */
public class BleConnParams {

	private static final String TAG = "BleConnParams";

	/** 连接超时时间 */
	private static final long DEF_CONNECT_TIMEOUT = 5000;

	/** 待连接的服务端设备 */
	private BluetoothDevice device;
	/** 服务UUID */
	private UUID serviceUuid;
	/** 读特征UUID */
	private UUID characteristicReadUuid;
	/** 写特征UUID */
	private UUID characteristicWriteUuid;
	/** 连接超时时间 */
	private long connectTimeout;

	/**
	 * @param device 待连接的服务端设备
	 * @throws IllegalArgumentException if device is null
	 */
	public BleConnParams(BluetoothDevice device, long connectTimeout) {
		init(device, connectTimeout, null, null, null);
	}

	/**
	 * @param remoteAddress 设备mac地址
	 * @throws IllegalArgumentException if device is null
	 */
	public BleConnParams(String remoteAddress) {
		device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(remoteAddress);
		init(device, DEF_CONNECT_TIMEOUT, null, null, null);
	}

	/**
	 * @param remoteAddress  设备mac地址
	 * @param connectTimeout 连接超时时间
	 * @throws IllegalArgumentException if device is null
	 */
	public BleConnParams(String remoteAddress, long connectTimeout) {
		device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(remoteAddress);
		init(device, connectTimeout, null, null, null);
	}

	private void init(BluetoothDevice device, long connectTimeout, String serviceUuid, String characteristicReadUuid, String characteristicWriteUuid) throws IllegalArgumentException {
		setDevice(device);
		setConnectTimeout(connectTimeout);
		setServiceUuid(serviceUuid);
		setCharacteristicReadUuid(characteristicReadUuid);
		setCharacteristicWriteUuid(characteristicWriteUuid);
	}

	/**
	 * @param device 待连接的服务端设备
	 * @throws IllegalArgumentException if device is null
	 */
	private void setDevice(BluetoothDevice device) throws IllegalArgumentException {
		if (device == null) {
			throw new IllegalArgumentException("server device can not be null");
		}
		this.device = device;
	}

	/**
	 * 设置待读写的服务uuid<br>
	 * 如果不设置则采用默认的uuid{@see #com.newland.global.Constant.UUID_SERVICE}
	 *
	 * @param serviceUuid 待读写的服务uuid
	 */
	public void setServiceUuid(String serviceUuid) {
		if (serviceUuid == null) {
			this.serviceUuid = UUID.fromString(Constant.UUID_SERVICE);
		} else {
			this.serviceUuid = UUID.fromString(serviceUuid);
		}
	}

	/**
	 * 设置待读的特征uuid<br>
	 * 如果不设置则采用默认的uuid{@see #com.newland.global.Constant.UUID_CHARACTERISTIC_READ}
	 *
	 * @param characteristicUuid 待读的特征uuid
	 */
	public void setCharacteristicReadUuid(String characteristicUuid) {
		if (characteristicUuid == null) {
			this.characteristicReadUuid = UUID.fromString(Constant.UUID_CHARACTERISTIC_READ);
		} else {
			this.characteristicReadUuid = UUID.fromString(characteristicUuid);
		}
	}

	/**
	 * 设置待写的特征uuid<br>
	 * 如果不设置则采用默认的uuid{@see #com.newland.global.Constant.UUID_CHARACTERISTIC_WRITE}
	 *
	 * @param characteristicUuid 待写的特征uuid
	 */
	public void setCharacteristicWriteUuid(String characteristicUuid) {
		if (characteristicUuid == null) {
			this.characteristicWriteUuid = UUID.fromString(Constant.UUID_CHARACTERISTIC_WRITE);
		} else {
			this.characteristicWriteUuid = UUID.fromString(characteristicUuid);
		}
	}

	/**
	 * 通过ble连接器获取service
	 *
	 * @param bluetoothGatt ble连接器
	 */
	public BluetoothGattService getBleGattService(BluetoothGatt bluetoothGatt) {
		if (bluetoothGatt == null) {
			Log.e(TAG, "bluetoothGatt is null");
			return null;
		}
		BluetoothGattService gattService = bluetoothGatt.getService(serviceUuid);
		return gattService;
	}

	/**
	 * 通过ble连接器和service的uuid和characteristic的uuid获取characteristic
	 *
	 * @param bluetoothGatt ble连接器
	 * @param readOrWrite   获取读或写的特征值
	 * @return ble特征值
	 */
	public BluetoothGattCharacteristic getBleGattCharacteristic(BluetoothGatt bluetoothGatt, boolean readOrWrite) {
		BluetoothGattService gattService = getBleGattService(bluetoothGatt);
		if (gattService == null) {
			Log.e(TAG, "gattService not found " + serviceUuid.toString());
			return null;
		}
		UUID characteristicUuid = readOrWrite ? characteristicReadUuid : characteristicWriteUuid;
		BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(characteristicUuid);
		if (gattCharacteristic == null) {
			Log.e(TAG, "gattCharacteristic not found " + characteristicUuid.toString());
			return null;
		}
		return gattCharacteristic;
	}

	/**
	 * @return 连接超时时间
	 */
	public long getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * 设置连接超时时间，如果该时间小于0，则用默认连接时间代替
	 *
	 * @param connectTimeout 连接超时时间
	 */
	public void setConnectTimeout(long connectTimeout) {
		if (connectTimeout > 0) {
			this.connectTimeout = connectTimeout;
		} else {
			this.connectTimeout = DEF_CONNECT_TIMEOUT;
		}
	}

	/**
	 * 打印所有服务和特征的uuid
	 *
	 * @param bluetoothGatt ble连接器
	 */
	public void debugAllServiceAndCharacteristic(BluetoothGatt bluetoothGatt) {
		List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
		if (serviceList == null || serviceList.size() == 0) {
			Log.e(TAG, "ble has no service");
			return;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			BluetoothGattService service = serviceList.get(i);
			Log.i(TAG, "service_" + i + " : " + service.getUuid().toString());
			List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
			if (characteristicList != null && characteristicList.size() > 0) {
				for (int j = 0; j < characteristicList.size(); j++) {
					BluetoothGattCharacteristic characteristic = characteristicList.get(j);
					Log.i(TAG, "characteristic_" + j + " : " + characteristic.getUuid().toString() + ", " + characteristic.getProperties() + ", " + characteristic.getPermissions());
				}
			} else {
				Log.e(TAG, "service_" + i + " has no characteristic");
			}
		}
	}

	public BluetoothDevice getDevice() {
		return device;
	}

	public UUID getServiceUuid() {
		return serviceUuid;
	}

	public UUID getCharacteristicReadUuid() {
		return characteristicReadUuid;
	}

	public UUID getCharacteristicWriteUuid() {
		return characteristicWriteUuid;
	}
}
