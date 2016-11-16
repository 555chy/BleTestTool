package com.newland.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.newland.ble.callback.IBleScanCallback;

import java.util.ArrayList;

/**
 * Ble扫描筛选器<br/>
 * <ol>
 * 所需权限
 * <li>android.permission.BLUETOOTH</li>
 * <li>android.permission.BLUETOOTH_ADMIN</li>
 * <li>android.permission.ACCESS_COARSE_LOCATION</li>
 * </ol>
 * 
 * @author chy
 */
public class BleScanFilter {
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    /** 扫描回调函数 */
    private IBleScanCallback bleScanCallback;
    /** 自定义扫描参数 */
    private BleScanParams params;
    /** 是否正在扫描中 */
    private boolean isScanning = false;
    /** 等待扫描超时的线程 */
    private TimeoutThread scanTimeoutThread;
    /** API<21时使用的扫描回调函数 */
    private OldBleScanCallback oldBleScanCallback;
    /** API>=21时使用的BLE扫描器 */
    private BluetoothLeScanner bleScanner;
    /** API>=21时使用的扫描回调函数 */
    private NewBleScanCallback newBleScanCallback;


    public BleScanFilter(Context context, BleScanParams params) {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        this.params = (params == null ? new BleScanParams() : params);
        oldBleScanCallback = new OldBleScanCallback();
        newBleScanCallback = new NewBleScanCallback();
    }

    /**
     * @param isScanning 正在扫描还是已经停止扫描了
     */
    public void setScanning(boolean isScanning) {
        this.isScanning = isScanning;
        if (scanTimeoutThread != null) {
            scanTimeoutThread.setScanFinished(!isScanning);
        }
    }

    /**
     * @return 是否正在搜索
     */
    public boolean isScanning() {
        return isScanning;
    }

    /**
     * 打开蓝牙开关
     */
    public boolean enableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            return true;
        }
        return bluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    /**
     * @return 蓝牙是否已经开启
     */
    public boolean isBluetoothEnabled() {
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    /**
     * 设置扫描超时时间
     *
     * @param scanTimeout 扫描超时时间(单位:毫秒)
     */
    public void setScanTimeout(long scanTimeout) {
        params.setScanTimeout(scanTimeout * 1000);
    }

    /**
     * 开始扫描ble设备, 如果已经在扫描中，又调用了一次该函数会重新开始计时
     *
     * @return true表示扫描已经开启, false表示蓝牙未打开
     */
    public synchronized boolean startLeScan(IBleScanCallback bleScanCallback) {
        this.bleScanCallback = bleScanCallback;
        // 判断蓝牙是否已经打开
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            bleScanCallback.onBluetoothNotOpen();
            enableBluetooth();
            return false;
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            boolean startResult;
            // 开始扫描ble设备
            startResult = bluetoothAdapter.startLeScan(oldBleScanCallback);
            if (!startResult) {
                return false;
            }
        } else {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            bleScanner.startScan(newBleScanCallback);
        }
        // 如果超时线程已经开始(就是原本的状态就是扫描中),则想办法重置超时时间
        isScanning = true;
        if (bleScanCallback != null) {
            bleScanCallback.onScanStarted();
        }
        if (scanTimeoutThread != null && scanTimeoutThread.isAlive()) {
            scanTimeoutThread.reinit();
        } else {
            scanTimeoutThread = new TimeoutThread();
            scanTimeoutThread.start();
        }
        return true;
    }

    /**
     * 停止扫描ble设备, 根据needCallback, 判断是否需要触发{@link IBleScanCallback#onScanFinished}
     *
     * @param needCallback 是否需要回调
     */
    public synchronized void stopLeScan(boolean needCallback) {
        if (isScanning) {
            setScanning(false);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.stopLeScan(oldBleScanCallback);
            } else {
                bleScanner.stopScan(newBleScanCallback);
            }
        }
        if (bleScanCallback != null && needCallback) {
            bleScanCallback.onScanFinished();
        }
    }

    /**
     * 获取蓝牙设备名,如果名字为空,就用蓝牙地址代表名字
     *
     * @param device 蓝牙设备
     * @return 蓝牙设备名
     */
    public static String getBluetoothDeviceName(BluetoothDevice device) {
        return TextUtils.isEmpty(device.getName()) ? device.getAddress() : device.getName();
    }

    /**
     * 从蓝牙设备列表中获取所有蓝牙设备名(如果没有名字用地址代替)
     *
     * @param deviceList 蓝牙设备列表
     * @return 蓝牙设备名数组
     */
    public static String[] getDeviceNameArray(ArrayList<BluetoothDevice> deviceList) {
        if (deviceList == null || deviceList.size() == 0) {
            return null;
        }
        String[] deviceNameArray = new String[deviceList.size()];
        for (int i = 0; i < deviceList.size(); i++) {
            deviceNameArray[i] = getBluetoothDeviceName(deviceList.get(i));
        }
        return deviceNameArray;
    }

    /**
     * 根据蓝牙地址的唯一性，来将设备无重复的添加到设备列表中
     *
     * @param deviceList 设备列表
     * @param device     新设备
     * @return 是否添加到设备列表中
     */
    public static boolean addBluetoothDevice(ArrayList<BluetoothDevice> deviceList, BluetoothDevice device) {
        synchronized (deviceList) {
            for (int i = 0; i < deviceList.size(); i++) {
                if (deviceList.get(i).getAddress().equals(device.getAddress())) {
                    return false;
                }
            }
            return deviceList.add(device);
        }
    }

    /**
     * API<21时使用的扫描回调函数
     */
    class OldBleScanCallback implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            bleScanCallback.onScanResult(bluetoothDevice, rssi);
        }

    }

    /**
     * API>=21时使用的扫描回调函数
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class NewBleScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            bleScanCallback.onScanResult(result.getDevice(), result.getRssi());
        }

        @Override
        public void onScanFailed(int errorCode) {
            bleScanCallback.onScanFail(errorCode);
            if(bluetoothAdapter != null) {
                //一旦发生错误，除了重启蓝牙再没有其它解决办法
                bluetoothAdapter.disable();
                bluetoothAdapter.enable();
            }
        }
    }

    /**
     * 超时监听线程
     */
    class TimeoutThread extends Thread {

        /**
         * 休眠步长(单位:毫秒)
         */
        private static final int TIME_STEP = 100;

        /**
         * 剩余时间
         */
        private long leftTime;
        /**
         * 超时时间是否被重置
         */
        private boolean isReinitTimeout;
        /**
         * 扫描完成或被手动终止,触发后退出循环
         */
        private boolean isScanFinished;

        public TimeoutThread() {
            reinit();
        }

        /**
         * 重置超时时间,同时重置是否触发的变量
         */
        public void reinit() {
            this.isReinitTimeout = true;
            setScanFinished(false);
        }

        /**
         * 扫描完成或被手动终止,触发后退出循环 ,表示不再需要再监听超时,线程终止并调用触发器相应函数
         *
         * @param isScanFinished 是否扫描已经结束
         */
        public void setScanFinished(boolean isScanFinished) {
            this.isScanFinished = isScanFinished;
        }

        @Override
        public void run() {
            while ((!isScanFinished && leftTime > 0) || isReinitTimeout) {
                if (isReinitTimeout) {
                    isReinitTimeout = false;
                    leftTime = params.getScanTimeout();
                }
                if (leftTime > TIME_STEP) {
                    try {
                        Thread.sleep(TIME_STEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    leftTime -= TIME_STEP;
                } else {
                    try {
                        Thread.sleep(leftTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    leftTime = 0;
                }
            }
            if (!isScanFinished) {
                // 扫描超时
                stopLeScan(true);
            }
        }
    }
}
