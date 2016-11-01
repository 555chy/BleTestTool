package com.newland.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.newland.adapter.BleCharacteristicAdapter;
import com.newland.ble.BleConnector;
import com.newland.ble.BleScanFilter;
import com.newland.ble.callback.IBleCallback;
import com.newland.ble.callback.IBleScanCallback;
import com.newland.bletesttool.R;
import com.newland.global.Constant;
import com.newland.model.BleCharacteristicModel;
import com.newland.model.BleDeviceModel;
import com.newland.model.MsgModel;
import com.newland.model.MsgModel.Direction;
import com.newland.model.MsgSendModel;
import com.newland.model.MsgSendModel.EncodingType;
import com.newland.model.SendHistoryModel;
import com.newland.utils.HexConvertUtils;
import com.newland.utils.MyPref;
import com.newland.utils.MyUtils;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

	/** 消息显示框滑动控件 */
	private ScrollView msgSv;
	/** 消息显示框 */
	private TextView msgTv;
	/** 显示编码单选按钮组 */
	private RadioGroup encodingRg;
	/** 以utf8编码显示 */
	private RadioButton utf8Rb;
	/** 以16进制编码显示 */
	private RadioButton hexRb;
	/** 是否显示时间 */
	private CheckBox showTimeCb;
	/** 是否自动回发 */
	private CheckBox autoSendBackCb;
	/** 是否自动重连 */
	private CheckBox autoReconnectCb;
	/** 尝试重连时间间隔 */
	private Spinner reconnectIntervalSpinner;
	/** 扫描超时时间 */
	private Spinner scanTimeoutSpinner;
	/** 显示UUID设置对话框 */
	private Button uuidSettingBtn;
	/** 检测并弹窗选择UUID按钮 */
	private Button uuidSelectBtn;
	/** 连接时是否弹窗选择UUID */
	private CheckBox neverSelectUuidWhenConnectCb;
	/** 显示发送对话框 */
	private Button sendSettingBtn;
	/** 显示发送历史记录对话框 */
	private Button sendHistoryBtn;
	/** 显示关于对话框 */
	private Button aboutBtn;
	/** 扫描BLE设备 */
	private Button scanDeviceBtn;
	/** 停止扫描，并弹出连接选择框 */
	private Button stopScanBtn;
	/** 删除设备 */
	private Button deleteDeviceBtn;
	/** 清空文本框 */
	private Button clearTextBtn;
	/** 接收(下行流量) */
	private TextView rxTv;
	/** 传送(上行流量) */
	private TextView txTv;

	private Context context = this;
	/** 以Utf8编码显示还是以Hex格式显示 */
	private boolean isUtf8;
	/** 是否显示接收时间 */
	private boolean isShowTime;
	/** 是否自动回发 */
	private boolean isAutoSendBack;
	/** 是否自动重连 */
	private boolean isAutoReconnect;
	/** 重连下拉框当前选中的索引号 */
	private int reconnectIntervalSelection;
	/** 重连间隔(隔几毫秒重连一次) */
	private long reconnectInterval;
	/** 扫描超时下拉框当前选中的索引号 */
	private int scanTimeoutSelection;
	/** 扫描超时时间(单位:毫秒) */
	private long scanTimeout;
	/** 以Utf8编码发送还是以Hex格式发送 */
	private boolean isSendUtf8;
	/** 当连接时,是否检测UUID(弹窗选择service和characteristic) */
	private boolean isDetectUuidWhenConnect;
	/** Ble服务UUID */
	private String serviceUuid;
	/** 读特征UUID */
	private String readUuid;
	/** 写特征UUID */
	private String writeUuid;
	/** 接收(下行流量)个数 */
	private int rxNum;
	/** 接收(上行流量)个数 */
	private int txNum;
	/** 接收(上行流量)失败个数 */
	private int txFailNum;
	/** 数据存储 */
	private MyPref myPref;
	/** 发送的历史记录 */
	private SendHistoryModel sendHistoryModel;
	/** 消息数据 */
	private ArrayList<MsgModel> msgList;
	/** 扫描到的BLE设备列表 */
	private ArrayList<BleDeviceModel> bleDeviceList;
	/** BLE扫描器 */
	private BleScanFilter bleScanFilter;
	/** BLE连接器 */
	private BleConnector bleConnector;
	/** BLE通信回调函数 */
	private IBleCallback bleCallback = new IBleCallback() {

        @Override
        public void onGetGattService(boolean result) {
            int strId = result ? R.string.succ_service_found : R.string.err_service_not_found;
            addLog(getResources().getString(strId) + " ( " + serviceUuid + " )");
        }

        @Override
        public void onGetReadCharacteristic(boolean result) {
            int strId = result ? R.string.succ_read_characteristic_found : R.string.err_read_characteristic_not_found;
            addLog(getResources().getString(strId) + " ( " + readUuid + " )");
        }

        @Override
        public void onGetWriteCharacteristic(boolean result) {
            int strId = result ? R.string.succ_write_characteristic_found : R.string.err_write_characteristic_not_found;
            addLog(getResources().getString(strId) + " ( " + writeUuid + " )");
        }

		@Override
		public void onDisconnect(boolean isManual) {
			addLog(getResources().getString(isManual ? R.string.active : R.string.passive) + getResources().getString(R.string.disconnect));
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					sendSettingBtn.setEnabled(false);
				}
			});
			if (!isAutoReconnect) {
				deleteDevice();
			}
		}

		@Override
		public void onRead(byte[] value) {
			MsgSendModel model = new MsgSendModel(isUtf8 ? EncodingType.UTF8 : EncodingType.HEX, value);
			addText(model, Direction.RECEIVED);
			rxNum++;
			resetRx();
			if (isAutoSendBack) {
				sendMsg(model);
			}
		}

		@Override
		public void onWriteReturn(boolean result, String errMsg) {
			if (result) {
				addLog(R.string.send_succ);
			} else {
				if (errMsg == null) {
					addLog(R.string.send_fail);
				} else {
					addLog(getResources().getString(R.string.send_fail) + " : " + errMsg);
				}
			}
			txNum++;
			if(!result) {
				txFailNum++;
			}
			resetTx();
		}

		@Override
		public void onReconnectStart() {
			addLog(R.string.reconnect_start);
		}

		@Override
		public void onReconnecting() {
			addLog(R.string.reconnecting);
		}

		@Override
		public void onReconnectEnd() {
			addLog(R.string.reconnect_stop);
		}

		@Override
		public void onReconnectStateChange(boolean isConnected, String errMsg) {
			if (isConnected) {
				addLog(R.string.reconnect_succ);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						sendSettingBtn.setEnabled(false);
					}
				});
			} else {
				if (errMsg == null) {
					addLog(R.string.reconnect_fail);
				} else {
					addLog(getResources().getString(R.string.reconnect_fail) + " : " + errMsg);
				}
			}
		}

		@Override
		public void log(String log) {
			addLog(log);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findView();
		setListener();
		initData();
		setData();
	}

	private void findView() {
		msgSv = (ScrollView) findViewById(R.id.msg_sv);
		msgTv = (TextView) findViewById(R.id.msg_tv);
		encodingRg = (RadioGroup) findViewById(R.id.encoding_rg);
		utf8Rb = (RadioButton) findViewById(R.id.utf8_rb);
		hexRb = (RadioButton) findViewById(R.id.hex_rb);
		showTimeCb = (CheckBox) findViewById(R.id.show_time_cb);
		autoSendBackCb = (CheckBox) findViewById(R.id.send_back_cb);
		autoReconnectCb = (CheckBox) findViewById(R.id.auto_reconnect_cb);
		reconnectIntervalSpinner = (Spinner) findViewById(R.id.reconnect_interval_spinner);
		scanTimeoutSpinner = (Spinner) findViewById(R.id.scan_timeout_spinner);
		uuidSelectBtn = (Button) findViewById(R.id.uuid_select_btn);
		neverSelectUuidWhenConnectCb = (CheckBox) findViewById(R.id.never_select_uuid_cb);
		sendHistoryBtn = (Button) findViewById(R.id.send_history_btn);
		sendSettingBtn = (Button) findViewById(R.id.send_setting_btn);
		uuidSettingBtn = (Button) findViewById(R.id.uuid_setting_btn);
		aboutBtn = (Button) findViewById(R.id.about_btn);
		scanDeviceBtn = (Button) findViewById(R.id.scan_device_btn);
		stopScanBtn = (Button) findViewById(R.id.stop_scan_btn);
		deleteDeviceBtn = (Button) findViewById(R.id.delete_device_btn);
		clearTextBtn = (Button) findViewById(R.id.clear_text_btn);
		rxTv = (TextView) findViewById(R.id.rx_tv);
		txTv = (TextView) findViewById(R.id.tx_tv);
	}

	/**
	 * 设置事件监听器
	 */
	private void setListener() {
		encodingRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup paramRadioGroup, int checkedId) {
				switch (checkedId) {
					case R.id.utf8_rb:
						isUtf8 = true;
						break;
					case R.id.hex_rb:
						isUtf8 = false;
						break;
				}
				myPref.putBoolean(Constant.SP_KEY_IS_UTF8, isUtf8);
			}
		});
		showTimeCb.setOnCheckedChangeListener(this);
		autoSendBackCb.setOnCheckedChangeListener(this);
		autoReconnectCb.setOnCheckedChangeListener(this);
		neverSelectUuidWhenConnectCb.setOnCheckedChangeListener(this);
		reconnectIntervalSpinner.setOnItemSelectedListener(this);
		scanTimeoutSpinner.setOnItemSelectedListener(this);
		uuidSettingBtn.setOnClickListener(this);
		uuidSelectBtn.setOnClickListener(this);
		sendHistoryBtn.setOnClickListener(this);
		sendSettingBtn.setOnClickListener(this);
		aboutBtn.setOnClickListener(this);
		scanDeviceBtn.setOnClickListener(this);
		stopScanBtn.setOnClickListener(this);
		deleteDeviceBtn.setOnClickListener(this);
		clearTextBtn.setOnClickListener(this);
	}
	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
		String key = null;
		switch(adapterView.getId()) {
			case R.id.reconnect_interval_spinner:
				key = Constant.SP_KEY_RECONN_INTERVAL_SELECTION;
				reconnectInterval = MyUtils.getSpinnerIntValue(reconnectIntervalSpinner, position) * 1000;
				bleConnector.setAutoReconnect(isAutoReconnect, reconnectInterval);
				break;
			case R.id.scan_timeout_spinner:
				key = Constant.SP_KEY_SCAN_TIMEOUT_SELECTION;
				scanTimeout = MyUtils.getSpinnerIntValue(scanTimeoutSpinner, position) * 1000;
				break;
		}
		if(key != null) {
			myPref.putInt(key, position);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> paramAdapterView) {
	}

	@Override
	public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean) {
		String key = null;
		switch (paramCompoundButton.getId()) {
			case R.id.show_time_cb:
				isShowTime = paramBoolean;
				key = Constant.SP_KEY_IS_SHOW_TIME;
				resetText();
				break;
			case R.id.send_back_cb:
				isAutoSendBack = paramBoolean;
				key = Constant.SP_KEY_IS_AUTO_SEND_BACK;
				break;
			case R.id.auto_reconnect_cb:
				isAutoReconnect = paramBoolean;
				key = Constant.SP_KEY_IS_AUTO_RECONN;
				bleConnector.setAutoReconnect(isAutoReconnect, reconnectInterval);
				break;
			case R.id.never_select_uuid_cb:
				isDetectUuidWhenConnect = !paramBoolean;
				//由于这个是相反的因此不能进入常规设置项
				myPref.putBoolean(Constant.SP_KEY_IS_DETECT_UUID_WHEN_CONNECT, isDetectUuidWhenConnect);
				bleConnector.setIsDetectUuidWhenConnect(isDetectUuidWhenConnect);
				return;
		}
		if (key != null) {
			myPref.putBoolean(key, paramBoolean);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.uuid_setting_btn:
				showUuidSettingDialog();
				break;
			case R.id.uuid_select_btn:
				showBleServicesDialog();
				break;
			case R.id.send_setting_btn:
				showSendSettingDialog();
				break;
			case R.id.send_history_btn:
				showSendHistoryDialog();
				break;
			case R.id.about_btn:
				showAboutDialog();
				break;
			case R.id.scan_device_btn:
				scanDevice();
				break;
			case R.id.stop_scan_btn:
				stopScan();
				break;
			case R.id.delete_device_btn:
				addLog(R.string.delete_device);
				deleteDevice();
				break;
			case R.id.clear_text_btn:
				clearText();
				break;
		}
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		myPref = MyPref.getInstance(context);
		isUtf8 = myPref.getBoolean(Constant.SP_KEY_IS_UTF8);
		isAutoSendBack = myPref.getBoolean(Constant.SP_KEY_IS_AUTO_SEND_BACK);
		isAutoReconnect = myPref.getBoolean(Constant.SP_KEY_IS_AUTO_RECONN);
		reconnectIntervalSelection = myPref.getInt(Constant.SP_KEY_RECONN_INTERVAL_SELECTION);
		reconnectInterval = MyUtils.getSpinnerIntValue(reconnectIntervalSpinner, reconnectIntervalSelection) * 1000;
		scanTimeoutSelection = myPref.getInt(Constant.SP_KEY_SCAN_TIMEOUT_SELECTION);
		scanTimeout = MyUtils.getSpinnerIntValue(scanTimeoutSpinner, scanTimeoutSelection) * 1000;
		isShowTime = myPref.getBoolean(Constant.SP_KEY_IS_SHOW_TIME);
		isSendUtf8 = myPref.getBoolean(Constant.SP_KEY_IS_SEND_UTF8);
		serviceUuid = myPref.getString(Constant.SP_KEY_UUID_SERVICE);
		if (serviceUuid == null) {
			serviceUuid = Constant.UUID_SERVICE;
		}
		readUuid = myPref.getString(Constant.SP_KEY_UUID_READ);
		if (readUuid == null) {
			readUuid = Constant.UUID_CHARACTERISTIC_READ;
		}
		writeUuid = myPref.getString(Constant.SP_KEY_UUID_WRITE);
		if (writeUuid == null) {
			writeUuid = Constant.UUID_CHARACTERISTIC_WRITE;
		}
		isDetectUuidWhenConnect = myPref.getBoolean(Constant.SP_KEY_IS_DETECT_UUID_WHEN_CONNECT);
		String sendHistoryStr = myPref.getString(Constant.SP_KEY_SEND_HISTORY);
		sendHistoryModel = new SendHistoryModel(sendHistoryStr);
		msgList = new ArrayList<MsgModel>();
		bleDeviceList = new ArrayList<BleDeviceModel>();
		bleScanFilter = new BleScanFilter(context, null);
		bleConnector = new BleConnector(context, bleCallback, isAutoReconnect, reconnectInterval);
	}

	/**
	 * 将数据绑定到控件上
	 */
	private void setData() {
		utf8Rb.setChecked(isUtf8);
		hexRb.setChecked(!isUtf8);
		autoSendBackCb.setChecked(isAutoSendBack);
		autoReconnectCb.setChecked(isAutoReconnect);
		reconnectIntervalSpinner.setSelection(reconnectIntervalSelection);
		scanTimeoutSpinner.setSelection(scanTimeoutSelection);
		showTimeCb.setChecked(isShowTime);
		neverSelectUuidWhenConnectCb.setChecked(!isDetectUuidWhenConnect);
		deleteDevice();
		clearText();
	}

	@Override
	protected void onStop() {
		try {
			myPref.putString(Constant.SP_KEY_SEND_HISTORY, sendHistoryModel.toSerializeStr());
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onStop();
	}

	/**
	 * 分别设置"扫描设备"、"停止搜索"、"删除设备"、"发送设置", 4个按钮的启用状态
	 *
	 * @param enableStart
	 *            是否启用"扫描设备"按钮
	 * @param enableStop
	 *            是否启用"停止搜索"按钮
	 * @param enableDelete
	 *            是否启用"删除设备"按钮
	 */
	private void setBleBtnEnabled(final boolean enableStart, final boolean enableStop, final boolean enableDelete, final boolean enableSendSetting) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				scanDeviceBtn.setEnabled(enableStart);
				stopScanBtn.setEnabled(enableStop);
				deleteDeviceBtn.setEnabled(enableDelete);
				sendSettingBtn.setEnabled(enableSendSetting);
				uuidSelectBtn.setEnabled(enableSendSetting);
			}
		});
	}

	/**
	 * 显示"设置发送信息"对话框
	 */
	private void showSendSettingDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_setting_send, null);
		final EditText sendMsgEt = (EditText) view.findViewById(R.id.msg_et);
		final RadioGroup sendEncodingRg = (RadioGroup) view.findViewById(R.id.encoding_rg);
		final RadioButton sendUtf8Rg = (RadioButton) view.findViewById(R.id.utf8_rb);
		final RadioButton sendHexRg = (RadioButton) view.findViewById(R.id.hex_rb);
		sendUtf8Rg.setChecked(isSendUtf8);
		sendHexRg.setChecked(!isSendUtf8);
		if (isSendUtf8) {
			sendMsgEt.setHint(R.string.hint_utf8);
		} else {
			sendMsgEt.setHint(R.string.hint_hex);
		}
		sendEncodingRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup paramRadioGroup, int checkId) {
				switch (checkId) {
				case R.id.utf8_rb:
					isSendUtf8 = true;
					sendMsgEt.setHint(R.string.hint_utf8);
					break;
				case R.id.hex_rb:
					isSendUtf8 = false;
					sendMsgEt.setHint(R.string.hint_hex);
					break;
				}
				myPref.putBoolean(Constant.SP_KEY_IS_SEND_UTF8, isSendUtf8);
			}
		});
		new AlertDialog.Builder(context).setTitle(R.string.send_setting).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int which) {
				String msg = sendMsgEt.getText().toString();
				byte[] buffer = null;
				if (isSendUtf8) {
					try {
						buffer = msg.trim().getBytes(Constant.CHARSET_NAME);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						addLog(e.getMessage());
					}
				} else {
					try {
						buffer = HexConvertUtils.hexStrToByteArr(context, msg);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						addLog(e.getMessage());
					}
				}
				if (buffer != null) {
					MsgSendModel model = new MsgSendModel(isSendUtf8 ? EncodingType.UTF8 : EncodingType.HEX, buffer);
					sendHistoryModel.addSendModel(model);
					sendMsg(model);
				}
			}
		}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * 显示"设置uuid对话框"
	 */
	private void showUuidSettingDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_setting_uuid, null);
		final EditText serviceUuidEt = (EditText) view.findViewById(R.id.service_uuid_et);
		final EditText readUuidEt = (EditText) view.findViewById(R.id.read_uuid_et);
		final EditText writeUuidEt = (EditText) view.findViewById(R.id.write_uuid_et);

		serviceUuidEt.setText(serviceUuid);
		readUuidEt.setText(readUuid);
		writeUuidEt.setText(writeUuid);
		new AlertDialog.Builder(context).setTitle(R.string.setting_uuid).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int which) {
				String tmpServiceUuid = serviceUuidEt.getText().toString();
				String tmpReadUuid = readUuidEt.getText().toString();
				String tmpWriteUuid = writeUuidEt.getText().toString();
				String log = "";
				if (!MyUtils.verifyUuid(tmpServiceUuid)) {
					log = getResources().getString(R.string.err_uuid_format_service);
				}
				if (!MyUtils.verifyUuid(tmpReadUuid)) {
					if (!TextUtils.isEmpty(log)) {
						log += "\n";
					}
					log += getResources().getString(R.string.err_uuid_format_read);
				}
				if (!MyUtils.verifyUuid(tmpWriteUuid)) {
					if (!TextUtils.isEmpty(log)) {
						log += "\n";
					}
					log += getResources().getString(R.string.err_uuid_format_write);
				}
				if (TextUtils.isEmpty(log)) {
					serviceUuid = tmpServiceUuid;
					readUuid = tmpReadUuid;
					writeUuid = tmpWriteUuid;
					setServiceUuid(serviceUuid);
					setReadUuid(readUuid);
					setWriteUuid(writeUuid);
					log = getResources().getString(R.string.set_succ);
				}
				addLog(log);
			}
		}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * 存储并更新BLE连接器service的UUID
	 */
	private void setServiceUuid(String uuid) {
		serviceUuid = uuid;
		myPref.putString(Constant.SP_KEY_UUID_SERVICE, serviceUuid);
		bleConnector.setServiceUuid(uuid);
	}

	/**
	 * 存储并更新BLE连接器ReadCharacteristic的UUID
	 */
	private void setReadUuid(String uuid) {
		readUuid = uuid;
		myPref.putString(Constant.SP_KEY_UUID_READ, readUuid);
		bleConnector.setCharacteristicReadUuid(uuid);
	}

	/**
	 * 存储并更新BLE连接器WriteCharacteristic的UUID
	 */
	private void setWriteUuid(String uuid) {
		writeUuid = uuid;
		myPref.putString(Constant.SP_KEY_UUID_WRITE, writeUuid);
		bleConnector.setCharacteristicWriteUuid(uuid);
	}

	/**
	 * 显示"关于"对话框
	 */
	private void showAboutDialog() {
		String aboutContent;
		try {
			aboutContent = MyUtils.getAssetsFileText(context, Constant.FILE_NAME_ABOUT);
		} catch (IOException e) {
			e.printStackTrace();
			aboutContent = getResources().getString(R.string.err_get_about);
		}
		new AlertDialog.Builder(context).setTitle(R.string.about).setMessage(aboutContent).setPositiveButton(R.string.ok, null).show();
	}

	/**
	 * 开始查找BLE设备
	 */
	private void scanDevice() {
		bleScanFilter.setScanTimeout(scanTimeout);
		bleScanFilter.startLeScan(new IBleScanCallback() {

			@Override
			public void onBluetoothNotOpen() {
				addLog(R.string.err_bluetooth_is_not_enabled);
			}

			@Override
			public void onScanStarted() {
				setBleBtnEnabled(false, true, false, false);
				bleDeviceList.clear();
				addLog(R.string.scan_device_start);
			}

			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				BleDeviceModel model = new BleDeviceModel(context, device, rssi);
				if (!bleDeviceList.contains(model)) {
					bleDeviceList.add(model);
					addLog(model.toSingleLineString());
				}
			}

			@Override
			public void onScanFinished() {
				addLog(R.string.scan_device_stop);
				showBleScanList();
				setBleBtnEnabled(true, true, false, false);
			}
		});
	}

	/**
	 * 停止搜索并显示连接对话框
	 */
	private void stopScan() {
		bleScanFilter.stopLeScan(true);
	}

	/**
	 * 显示BLE列表
	 */
	private void showBleScanList() {
		Collections.sort(bleDeviceList);
		final String[] bleDeviceArray = new String[bleDeviceList.size()];
		for (int i = 0; i < bleDeviceList.size(); i++) {
			bleDeviceArray[i] = bleDeviceList.get(i).toMultiLineString();
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new AlertDialog.Builder(context).setTitle(R.string.scan_result).setItems(bleDeviceArray, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int position) {
						final BleDeviceModel device = bleDeviceList.get(position);
						final String deviceStr = device.toSingleLineString();
						addLog(getResources().getString(R.string.select) + " : " + deviceStr);
						addLog(R.string.try_connecting);
						new Thread(new Runnable() {

							@Override
							public void run() {
								connectDevice(device.getAddress(), deviceStr);
							}
						}).start();

					}
				}).show();
			}
		});
	}

	/**
	 * 删除已连接的设备
	 */
	private void deleteDevice() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				bleDeviceList.clear();
				bleScanFilter.stopLeScan(false);
				bleConnector.close();
			}
		}).start();
		setBleBtnEnabled(true, false, false, false);
	}

	/**
	 * 根据设备地址连接设备
	 *
	 * @param address
	 *            设备的MAC地址
	 * @param deviceStr
	 *            设备的描述字符串
	 */
	private void connectDevice(String address, String deviceStr) {
		bleConnector.setIsDetectUuidWhenConnect(isDetectUuidWhenConnect);
		bleConnector.setParams(address, serviceUuid, readUuid, writeUuid);
		try {
			bleConnector.connect(false);
			// 连接成功
			addLog(getResources().getString(R.string.connect_succ) + " : " + deviceStr);
			setBleBtnEnabled(false, false, true, true);
			if(isDetectUuidWhenConnect) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showBleServicesDialog();
					}
				});
			} else {
				bleConnector.detectServiceAndCharacteristic();
			}
		} catch (Exception e) {
			e.printStackTrace();
			addLog(e.getMessage());
		}
	}

	/**
	 * 连上设备后，检索Gatt服务，显示Gatt服务的UUID的单选列表对话框
	 */
	private void showBleServicesDialog() {
		final String[] serviceUuids = bleConnector.getServiceUuids();
		if(serviceUuids == null) {
			addLog(R.string.err_service_not_found_any);
		} else {
			new AlertDialog.Builder(context).setTitle(R.string.select_service).setItems(serviceUuids, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					setServiceUuid(serviceUuids[i]);
//					addLog(context.getResources().getString(R.string.select) + context.getResources().getString(R.string.service_uuid) + " : " + serviceUuids[i]);
					showBleCharacteristicsDialog();
				}
			}).setCancelable(false).show();
		}
	}

	/**
	 * 连上设备后，检索Gatt服务后，选择待读写的Gatt特征值的UUID
	 */
	private void showBleCharacteristicsDialog() {
		BleCharacteristicModel[] characteristics = bleConnector.getCharacteristics();
		if(characteristics == null) {
			addLog(R.string.err_characteristic_not_found_any);
		} else {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.dialog_setting_characteristics, null);
			ListView characteristicLv = (ListView) view.findViewById(R.id.characteristic_lv);
			final BleCharacteristicAdapter adapter = new BleCharacteristicAdapter(context, characteristics);
			characteristicLv.setAdapter(adapter);
			String title = getResources().getString(R.string.select_read_write_characteristic) + "\n(" + getResources().getString(R.string.service_uuid) + ":" + serviceUuid + ")";
			new AlertDialog.Builder(context).setTitle(title).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					String readUuid = adapter.getReadUuid();
					String writeUuid = adapter.getWriteUuid();
					if(readUuid != null) {
						setReadUuid(readUuid);
//						addLog(context.getResources().getString(R.string.select) + context.getResources().getString(R.string.read_uuid) + " : " + readUuid);
					} else {
						addLog(R.string.read_characteristic_not_select);
					}
					if(writeUuid != null) {
						setWriteUuid(writeUuid);
//						addLog(context.getResources().getString(R.string.select) + context.getResources().getString(R.string.write_uuid) + " : " + writeUuid);
					} else {
						addLog(R.string.write_characteristic_not_select);
					}
				}
			}).setCancelable(false).show();
		}
	}

	/**
	 * 发送消息
	 */
	private void sendMsg(final MsgSendModel model) {
		if (model == null) {
			return;
		}
		if(!bleConnector.isConnectedAndInitSucc()) {
			addLog(R.string.err_ble_connect_nothing);
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					bleConnector.write(model.getMsg());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
		sendHistoryModel.addSendModel(model);
		addText(model, Direction.SEND);
	}

	/**
	 * 显示"发送历史记录"对话框,让用户中选择相应选项重新发送
	 */
	private void showSendHistoryDialog() {
		String[] sendHistoryArray = sendHistoryModel.toStrArray(context);
		if (sendHistoryArray == null) {
			addLog(R.string.err_send_history_is_empty);
		} else {
			// 这里有必要复制出一套副本,因为历史记录随时可能改变
			final ArrayList<MsgSendModel> sendHistoryList = (ArrayList<MsgSendModel>) sendHistoryModel.getSendHistoryList().clone();
			new AlertDialog.Builder(context).setTitle(R.string.send_history).setItems(sendHistoryArray, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface paramDialogInterface, int position) {
					MsgSendModel model = sendHistoryList.get(position);
					sendMsg(model);
				}
			}).show();
		}
	}

	/**
	 * 将"新的消息"添加到文本框中
	 */
	private void addText(MsgModel model) {
		synchronized (msgList) {
			msgList.add(model);
		}
		final String str = model.toString(context, isShowTime);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				msgTv.setText(msgTv.getText().toString() + str + "\n");
				msgSv.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	/**
	 * 将"新的消息"添加到文本框中, 并记录该消息
	 */
	private void addText(MsgSendModel model, Direction direction) {
		MsgModel msgModel = new MsgModel(model, direction);
		addText(msgModel);
	}

	/**
	 * 将"日志信息"添加到文本框中
	 */
	private void addLog(int logStrId) {
		addLog(getResources().getString(logStrId));
	}

	/**
	 * 将"日志信息"添加到文本框中
	 */
	private void addLog(String log) {
		MsgModel model = new MsgModel(log);
		addText(model);
	}

	/**
	 * 由于是否显示时间这一项变了，所以要重新设置文本框文本
	 */
	private void resetText() {
		final StringBuilder sb = new StringBuilder();
		synchronized (msgList) {
			for (int i = 0; i < msgList.size(); i++) {
				MsgModel model = msgList.get(i);
				String str = model.toString(context, isShowTime);
				sb.append(str + "\n");
			}
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				msgTv.setText(sb.toString());
			}
		});
	}

	/**
	 * 设置接收数据统计值
	 */
	private void resetRx() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rxTv.setText(String.format(getResources().getString(R.string.rx_description), rxNum));
			}
		});
	}

	/**
	 * 设置发送数据统计值
	 */
	private void resetTx() {
		final int txSuccNum = txNum - txFailNum;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				txTv.setText(String.format(getResources().getString(R.string.tx_description), txNum, txSuccNum, txFailNum));
			}
		});
	}

	/**
	 * 清空文本框
	 */
	private void clearText() {
		synchronized (msgList) {
			msgList.clear();
		}
		msgTv.setText("");
		rxNum = 0;
		txNum = 0;
		txFailNum = 0;
		resetRx();
		resetTx();
	}
}
