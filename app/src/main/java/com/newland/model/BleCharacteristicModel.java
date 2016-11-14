package com.newland.model;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.newland.bletesttool.R;

public class BleCharacteristicModel {

    private String uuid;
    private int properties;

    public BleCharacteristicModel(BluetoothGattCharacteristic bleGattCharacteristic) {
        uuid = bleGattCharacteristic.getUuid().toString();
        properties = bleGattCharacteristic.getProperties();
    }

    public String getUuid() {
        return uuid;
    }

    /**
     * 根据属性掩码判断并添加相应属性
     */
    private boolean addPropertiesStr(StringBuilder sb, int propertyMask, String propertiesStr) {
        if((properties & propertyMask) != 0) {
            sb.append("\"");
            sb.append(propertiesStr);
            sb.append("\" ");
            return true;
        }
        return false;
    }

    /**
     * 获取特性的描述信息
     */
    private StringBuilder getPropertiesDescription(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getResources().getString(R.string.property));
        sb.append(" ( ");
        boolean isAdd = false;
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_BROADCAST, "Broadcast");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS, "Extended props");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_INDICATE, "Indicate");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_NOTIFY, "Notify");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_READ, "Read");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, "Signed write");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_WRITE, "Write");
        isAdd |= addPropertiesStr(sb, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, "Write no response");
        if(!isAdd) {
            sb.append(context.getResources().getString(R.string.none));
            sb.append(" ");
        }
        sb.append(")");
        return sb;
    }

    public String toString(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(uuid);
        sb.append("\n");
        sb.append(getPropertiesDescription(context));
        return sb.toString();
    }
}
