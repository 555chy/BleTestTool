package com.newland.model;

import com.newland.bletesttool.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * 存放扫描到的BLE设备信息
 */
public class BleDeviceModel implements Comparable<BleDeviceModel> {

	/** 设备名称 */
	private String name;
	/** 设备MAC地址 */
	private String address;
	/** 发射信号强度 */
	private int rssi;

	public BleDeviceModel(Context context, BluetoothDevice device, int rssi) {
		name = device.getName();
		if (TextUtils.isEmpty(name)) {
			name = context.getResources().getString(R.string.unknown);
		}
		address = device.getAddress();
		this.rssi = rssi;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public int getRssi() {
		return rssi;
	}

	public String toSingleLineString() {
		return "(" + rssi + ") " + name + "   " + address;
	}

	public String toMultiLineString() {
		return "(" + rssi + ") " + name + "\n" + address;
	}

	@Override
	public int compareTo(@NonNull BleDeviceModel another) {
		return another.getRssi() - rssi;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BleDeviceModel) {
			return address.equals(((BleDeviceModel) obj).address);
		}
		return false;
	}
}
