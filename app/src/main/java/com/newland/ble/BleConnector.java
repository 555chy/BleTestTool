package com.newland.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.newland.ble.callback.BleGattCallbackImpl;
import com.newland.ble.callback.IBleCallback;
import com.newland.ble.callback.IConnectionChangeCallback;
import com.newland.bletesttool.R;
import com.newland.global.Constants;
import com.newland.model.BleCharacteristicModel;

import java.util.List;

/**
 * Ble连接器
 * 
 * @since at least Android API 18
 * @author chy
 */
public class BleConnector {

	/** 是否已经连接 */
	private boolean isConnected;
	/** 是否是手动断开(初始化时为true,连接上后该为false) */
	private boolean isManualDisconnect;
	/** 是否正在自动重连 */
	private boolean isReconnecting;
	/** 是否自动重连 */
	private boolean isAutoReconnect;
	/** 重连时间间隔(单位:毫秒) */
	private long reconnectInterval;
	/** 在连接时是否自动检测UUID,并弹窗供用户选择 */
	private boolean isDetectUuidWhenConnect;
	/** 搜索服务是否成功 */
	private boolean isServiceDiscovered;
	/** 是否发现读特征 */
	private boolean isReadCharacteristicFound;
	/** 是否发现读特征 */
	private boolean isWriteCharacteristicFound;
	/** 是否已开启通知 */
	private boolean isEnableNotification;

	private Context context;
	/** ble连接参数 */
	private BleConnParams params;
	/** ble连接、读、写回调 */
	private BleGattCallbackImpl bleGattCallback;
	/** 同步锁 */
	private final Object lockObj = new Object();
	/** ble连接器 */
	private BluetoothGatt bluetoothGatt;
	/** ble通讯工具 */
	private BleConnection bleConnection;
	/** 提供给界面端的回调函数 */
	private IBleCallback bleCallback;
	/** 连接状态改变回调 */
	private IConnectionChangeCallback callback = new IConnectionChangeCallback() {

		@Override
		public void onConnectFinish(boolean connectResult, boolean discoveredResult, BluetoothGatt gatt) {
			isConnected = connectResult;
			if (!connectResult) {
				return;
			}
			isServiceDiscovered = discoveredResult;
			if (isServiceDiscovered) {
				bluetoothGatt = gatt;
			}
			synchronized (lockObj) {
				lockObj.notifyAll();
			}
		}

		@Override
		public void onDisconnect() {
			if (isConnected) {
				isConnected = false;
				bleConnection.close();
				bleCallback.onDisconnect(isManualDisconnect);
				if (!isManualDisconnect) {
					reconnect();
				}
			}
		}
	};

	public BleConnector(Context context, IBleCallback bleCallback, boolean isAutoReconnect, long reconnectInterval) throws IllegalArgumentException {
		// 必须在初始化时设置其标志，是为了避免断线重连
		isManualDisconnect = true;
		if (bleCallback == null) {
			throw new IllegalArgumentException(context.getResources().getString(R.string.err_callback_func_cannot_be_null));
		}
		this.context = context;
		this.bleCallback = bleCallback;
		setAutoReconnect(isAutoReconnect, reconnectInterval);
	}

	/**
	 * 返回是否连接成功，并执行完相应初始化设置和操作
	 */
	public boolean isConnectedAndInitSucc() {
//		return (isConnected && isServiceDiscovered && isCharacteristicFound && isEnableNotification);
		return (isConnected && isServiceDiscovered);
	}

	/**
	 * 设置超时重连
	 *
	 * @param isAutoReconnect   当连接被动断开时，是否启动重连机制
	 * @param reconnectInterval 重连时间间隔(单位:毫秒)
	 */
	public void setAutoReconnect(boolean isAutoReconnect, long reconnectInterval) {
		this.isAutoReconnect = isAutoReconnect;
		this.reconnectInterval = reconnectInterval;
		reconnect();
	}

	/**
	 * 在连接时是否自动检测UUID,并弹窗供用户选择
	 */
	public void setIsDetectUuidWhenConnect(boolean isDetectUuidWhenConnect) {
		this.isDetectUuidWhenConnect = isDetectUuidWhenConnect;
	}

	/**
	 * 设置连接参数
	 */
	public void setParams(String address, String serviceUuid, String readUuid, String writeUuid) {
		params = new BleConnParams(address);
		setParams(serviceUuid, readUuid, writeUuid);
	}

	/**
	 * 设置连接参数
	 */
	public void setParams(String serviceUuid, String readUuid, String writeUuid) {
		setServiceUuid(serviceUuid);
		setCharacteristicReadUuid(readUuid);
		setCharacteristicWriteUuid(writeUuid);
	}

	/**
	 * 设置服务UUID
	 */
	public void setServiceUuid(String uuid) {
		params.setServiceUuid(uuid);
		if(isConnected) {
			bleCallback.onGetGattService(params.getBleGattService(bluetoothGatt) != null);
		}
	}

	/**
	 * 开启characteristic的Notification
	 */
	private void setCharacteristicNotification(BluetoothGattCharacteristic characteristicRead) {
		if (characteristicRead != null) {
			isEnableNotification = bluetoothGatt.setCharacteristicNotification(characteristicRead, true);
			List<BluetoothGattDescriptor> descriptorList = characteristicRead.getDescriptors();
			bleCallback.log("BluetoothGattDescriptor count = " + descriptorList.size());
			if (descriptorList != null && descriptorList.size() > 0) {
				for (int i = 0; i < descriptorList.size(); i++) {
					BluetoothGattDescriptor descriptor = descriptorList.get(i);
					bleCallback.log("descriptorList["+i+"] uuid="+ descriptor.getUuid().toString()+", permission="+ descriptor.getPermissions());
					descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					bluetoothGatt.writeDescriptor(descriptor);
				}
			}
		}
	}

	/**
	 * 设置Notify特征UUID
	 */
	public void setCharacteristicReadUuid(String uuid) {
		params.setCharacteristicReadUuid(uuid);
		if(isConnected) {
			BluetoothGattCharacteristic characteristicRead = params.getBleGattCharacteristic(bluetoothGatt, true);
			bleCallback.onGetReadCharacteristic(characteristicRead != null);
			setCharacteristicNotification(characteristicRead);
		}
	}

	/**
	 * 设置Write特征UUID
	 */
	public void setCharacteristicWriteUuid(String uuid) {
		params.setCharacteristicWriteUuid(uuid);
		if(isConnected) {
			bleCallback.onGetWriteCharacteristic(params.getBleGattCharacteristic(bluetoothGatt, false) != null);
		}
	}

	/**
	 * 检测服务和特征UUID是否能获取到相应的Gatt对象
	 */
	public void detectServiceAndCharacteristic() {
		if(isConnected && !isDetectUuidWhenConnect) {
			BluetoothGattCharacteristic characteristicRead = params.getBleGattCharacteristic(bluetoothGatt, true);
			BluetoothGattCharacteristic characteristicWrite = params.getBleGattCharacteristic(bluetoothGatt, false);

			isReadCharacteristicFound = (characteristicRead != null);
			isWriteCharacteristicFound = (characteristicWrite != null);

			bleCallback.onGetGattService(params.getBleGattService(bluetoothGatt) != null);
			bleCallback.onGetReadCharacteristic(isReadCharacteristicFound);
			bleCallback.onGetWriteCharacteristic(isWriteCharacteristicFound);

			setCharacteristicNotification(characteristicRead);
		}
	}


	/**
	 * 阻塞函数, 用于连接服务. 如果连接失败, 则抛出异常, 否则成功
	 *
	 * @param isReconnect 该连接是否是重连
	 */
	public void connect(boolean isReconnect) throws Exception {
		// 如果不是重连，说明这是用户主动连接设备
		if (!isReconnect) {
			isManualDisconnect = true;
		}
		if (params == null) {
			throw new IllegalArgumentException(context.getResources().getString(R.string.err_ble_connect_param_cannot_be_null));
		}
		BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
		if (!bluetoothAdapter.isEnabled()) {
			throw new Exception(context.getResources().getString(R.string.err_bluetooth_is_not_enabled));
		}
		BluetoothDevice device = params.getDevice();
		bleGattCallback = new BleGattCallbackImpl(context, callback);
		bluetoothGatt = null;
		isConnected = false;
		synchronized (lockObj) {
			bluetoothGatt = device.connectGatt(context, false, bleGattCallback);
			try {
				lockObj.wait(params.getConnectTimeout());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		int errStrId;
		if (isConnectedAndInitSucc()) {
			isManualDisconnect = false;
			bleConnection = new BleConnection(context, bleGattCallback, bluetoothGatt, params, bleCallback);
			return;
		} else if (!isConnected) {
			errStrId = R.string.err_ble_connect_fail;
		} else if (!isServiceDiscovered) {
			errStrId = R.string.err_ble_discover_service_fail;
//		} else if (!isCharacteristicFound) {
//			errStrId = R.string.err_ble_found_characteristic_fail;
//		} else if (!isEnableNotification) {
//			errStrId = R.string.err_ble_enable_notification_fail;
		} else {
			errStrId = R.string.err_ble_connect_fail;
		}
		if (bluetoothGatt != null) {
			bluetoothGatt.disconnect();
		}
		throw new Exception(context.getResources().getString(errStrId));
	}

	/**
	 * 重连
	 */
	private void reconnect() {
		if (!isAutoReconnect || isConnected || isManualDisconnect || isReconnecting) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				isReconnecting = true;
				bleCallback.onReconnectStart();
				long leftTime = 0;
				while (!isConnected && !isManualDisconnect) {
					if (leftTime <= 0) {
						leftTime = reconnectInterval;
						try {
							bleCallback.onReconnecting();
							connect(true);
							bleCallback.onReconnectStateChange(true, null);
							break;
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							bleCallback.onReconnectStateChange(false, e.getMessage());
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						leftTime -= Constants.DETECT_TIME_INTERVAL;
					}
					try {
						Thread.sleep(Constants.DETECT_TIME_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				bleCallback.onReconnectEnd();
				isReconnecting = false;
			}
		}).start();
	}

	/**
	 * 获取服务的UUID
	 */
	public String[] getServiceUuids() {
		if(isConnected && bluetoothGatt != null) {
            List<BluetoothGattService> bleGattServiceList = bluetoothGatt.getServices();
            if (bleGattServiceList != null && bleGattServiceList.size() > 0) {
                String[] serviceUuids = new String[bleGattServiceList.size()];
                for (int i = 0; i < bleGattServiceList.size(); i++) {
                    serviceUuids[i] = bleGattServiceList.get(i).getUuid().toString();
                }
                return serviceUuids;
            }
        }
        return null;
	}

	/**
	 * 获取读特征的UUID
	 */
	public BleCharacteristicModel[] getCharacteristics() {
		if(isConnected && bluetoothGatt != null) {
            BluetoothGattService bleGattService = bluetoothGatt.getService(params.getServiceUuid());
            if(bleGattService != null) {
                List<BluetoothGattCharacteristic> bleGattCharacteristicList = bleGattService.getCharacteristics();
                if (bleGattCharacteristicList != null && bleGattCharacteristicList.size() > 0) {
                    BleCharacteristicModel[] characteristics = new BleCharacteristicModel[bleGattCharacteristicList.size()];
                    for (int i = 0; i < bleGattCharacteristicList.size(); i++) {
                        characteristics[i] = new BleCharacteristicModel(bleGattCharacteristicList.get(i));
                    }
                    return characteristics;
                }
            }
		}
        return null;
	}

	/**
	 * 写信息
	 */
	public void write(byte[] buffer) throws Exception {
		if (!isConnected || bleConnection == null) {
			throw new Exception(context.getResources().getString(R.string.err_ble_connect_nothing));
		}
		bleConnection.write(buffer);
	}

	/**
	 * 断开BLE连接
	 */
	public void close() {
		isManualDisconnect = true;
		if (isConnected) {
			if (bluetoothGatt != null) {
				bluetoothGatt.close();
				bleConnection.close();
				//主动调用close会把gatt直接关闭,因此就不会调用gatt的回调事件了
				bleCallback.onDisconnect(isManualDisconnect);
			}
			isConnected = false;
		}
	}
}
