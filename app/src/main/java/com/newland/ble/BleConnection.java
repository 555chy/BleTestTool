package com.newland.ble;

import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.newland.ble.callback.IBleCallback;
import com.newland.ble.callback.IExecResultCallback;
import com.newland.bletesttool.R;
import com.newland.utils.HexConvertUtils;

/**
 * Ble读写装置
 */
public class BleConnection {

	/** 每个包除去首部标志位,剩下的数据部分的最大长度 */
	private static final byte PACKAGE_DATA_LEN = 20;

	private Context context;
	/** ble读写器 */
	private BluetoothGatt bluetoothGatt;
	/** ble连接参数 */
	private BleConnParams bleConnParams;
	/** 写策略模型 */
	private WritePolicy writeIntervalPolicy = new WritePolicy();
	/** 提供给界面端的回调函数 */
	private IBleCallback bleCallback;
	/** ble断线、读写回调 */
	private IExecResultCallback execResultCallback = new IExecResultCallback() {

		@Override
		public void onWriteReturn(UUID uuid, boolean result) {
            if (!bleConnParams.getCharacteristicWriteUuid().equals(uuid)) {
                //  Log.w(TAG, "onWriteReturn deprecated, uuid is not equal, uuid = " + uuid);
                return;
            }
            writeIntervalPolicy.onExecReturn(result);
			writeIntervalPolicy.objNotify();
		}

		@Override
		public void onNotifyReturn(UUID uuid, byte[] value) {
            if (!bleConnParams.getCharacteristicReadUuid().equals(uuid)) {
                // Log.w(TAG, "onNotifyReturn deprecated, uuid is not equal, uuid = " + uuid);
                return;
            }
            if (value == null || value.length == 0) {
				return;
			}
			bleCallback.onRead(value);
		}
	};

	public BleConnection(Context context, BleGattCallback bleGattCallback, BluetoothGatt bluetoothGatt, BleConnParams bleConnParams, IBleCallback bleCallback) {
		this.context = context;
		this.bluetoothGatt = bluetoothGatt;
		this.bleConnParams = bleConnParams;
		this.bleCallback = bleCallback;
		bleGattCallback.setExecResultCallback(execResultCallback);
	}

	/**
	 * 阻塞方法, 将数据传给服务端
	 */
	public synchronized void write(byte[] buffer) {
		bleCallback.log("write all data=" + HexConvertUtils.byteArrToHexStr(context, buffer) + "\n" + "write divide package count = " + Math.ceil((float) buffer.length / (float) PACKAGE_DATA_LEN));
		BluetoothGattCharacteristic gattCharacteristic = bleConnParams.getBleGattCharacteristic(bluetoothGatt, false);
		if (gattCharacteristic == null) {
			bleCallback.onWriteReturn(false, context.getResources().getString(R.string.err_ble_found_characteristic_fail));
		}
		int offset = 0;
		writeIntervalPolicy.resetTryTimes();
		while (offset < buffer.length) {
			byte[] childBuffer = clipBuffer(buffer, offset);
			bleCallback.log("write prepare, buffer=" + HexConvertUtils.byteArrToHexStr(context, childBuffer));
			if(childBuffer == null || childBuffer.length == 0) {
				break;
			}
			gattCharacteristic.setValue(childBuffer);
			do {
				writeIntervalPolicy.resetExecStatus();
				boolean initiatedResult = bluetoothGatt.writeCharacteristic(gattCharacteristic);
				if (initiatedResult) {
					writeIntervalPolicy.objWait();
					if (writeIntervalPolicy.isExecSucc()) {
						offset += PACKAGE_DATA_LEN;
					} else {
						writeIntervalPolicy.onExecReturn(false);
					}
				} else {
					writeIntervalPolicy.onExecReturn(false);
					try {
						Thread.sleep(writeIntervalPolicy.getWriteInterval());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} while (!writeIntervalPolicy.isExecSucc() && !writeIntervalPolicy.isReachMaxFailTimes());
			if (writeIntervalPolicy.isReachMaxFailTimes()) {
				bleCallback.onWriteReturn(false, "write fail, has written " + offset + "/" + buffer.length);
				return;
			}
		}
		bleCallback.onWriteReturn(true, null);
	}

	/**
	 * 裁剪数据,从offset位置开始,最大的裁剪长度为{@link #PACKAGE_DATA_LEN}<br>
	 *
	 * @param buffer 数据
	 * @param offset 当前位置
	 */
	private byte[] clipBuffer(byte[] buffer, int offset) {
		int leftLen = buffer.length - offset;
		int childDataSize;
		if (leftLen > PACKAGE_DATA_LEN) {
			childDataSize = PACKAGE_DATA_LEN;
		} else {
			childDataSize = leftLen;
		}
		byte[] childBuffer = new byte[childDataSize];
		System.arraycopy(buffer, offset, childBuffer, 0, childDataSize);
		return childBuffer;
	}

	/**
	 * 写策略
	 */
	class WritePolicy {
		/** wait-notify等待时间 */
		private static final long WAIT_TIME = 500;
		/** 写的间隔时间(单位:毫秒) */
		private static final long WRITE_INTERVAL = 80;
		/** 最大尝试次数 */
		private static final byte MAX_TRY_TIMES = 1;

		/** 写的间隔时间(单位:毫秒) */
		private long writeInterval = WRITE_INTERVAL;
		/** 执行结果是否成功 */
		private boolean isExecSucc = false;
		/** 发送单个帧出错的尝试次数 */
		private byte tryTimes = 0;
		/** 判断是否已经得到了获取到返回结果 */
		private boolean isAlreadyGetStatus = false;
		/** 同步对象 */
		private final Object lockObj = new Object();

		/**
		 * @return 写的最大时间间隔(单位:毫秒)
		 */
		public long getWriteInterval() {
			return writeInterval;
		}

		/**
		 * 重置执行结果为false,并且设置未获得执行结果标记
		 */
		public void resetExecStatus() {
			isExecSucc = false;
			isAlreadyGetStatus = false;
		}

		/**
		 * @return 执行结果
		 */
		public boolean isExecSucc() {
			return isExecSucc;
		}

		/**
		 * 重置尝试次数为0
		 */
		public void resetTryTimes() {
			tryTimes = 0;
		}

		/**
		 * @return 最大尝试次数
		 */
		public byte getTryTimes() {
			return tryTimes;
		}

		/**
		 * @return 是否达到最大失败次数,如果是下一步可能采取行动终止写操作
		 */
		public boolean isReachMaxFailTimes() {
			return tryTimes >= MAX_TRY_TIMES;
		}

		/**
		 * 判断是否的到执行结果,亦或是超时才被判定为失败.如果已经获得过就直接忽略该函数,否则设置获得执行结果的标志位同时记录执行结果.
		 * 如果执行成功重置{@link #tryTimes}为0;否则{@link #tryTimes}自增
		 *
		 * @param result 执行结果
		 */
		public void onExecReturn(boolean result) {
			Log.e("", "onExecReturn " + result);
			if (isAlreadyGetStatus) {
				return;
			}
			isAlreadyGetStatus = true;
			this.isExecSucc = result;
			if (result) {
				tryTimes = 0;
			} else {
				tryTimes++;
			}
		}

		public void objWait() {
			synchronized (lockObj) {
				try {
					lockObj.wait(WAIT_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void objNotify() {
			synchronized (lockObj) {
				lockObj.notifyAll();
			}
		}
	}
}
