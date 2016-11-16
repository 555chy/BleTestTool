package com.newland.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.newland.adapter.BleCharacteristicAdapter;
import com.newland.ble.BleConnector;
import com.newland.ble.BleScanFilter;
import com.newland.ble.callback.IBleCallback;
import com.newland.ble.callback.IBleScanCallback;
import com.newland.bletesttool.R;
import com.newland.global.Constants;
import com.newland.manager.IntSpinnerManager;
import com.newland.manager.ViewUpdateManager;
import com.newland.model.BleCharacteristicModel;
import com.newland.model.BleDeviceModel;
import com.newland.model.EncodingType;
import com.newland.model.MsgList;
import com.newland.model.MsgLogModel;
import com.newland.model.MsgLogModel.LogLevel;
import com.newland.model.MsgModel;
import com.newland.model.MsgSendHistoryList;
import com.newland.model.MsgSendHistoryModel;
import com.newland.model.MsgSendRecvModel;
import com.newland.model.MsgSendRecvModel.Direction;
import com.newland.utils.FileUtils;
import com.newland.utils.HexConvertUtils;
import com.newland.utils.PrefUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, ViewUpdateManager.IUpdateCallback {

	/** 消息显示框滑动控件 */
//	private ScrollView msgSv;
	/** 消息显示框 */
//	private TextView msgTv;
	/** 显示编码单选按钮组 */
	private RadioGroup encodingRg;
	/** 以utf8编码显示 */
	private RadioButton utf8Rb;
	/** 以16进制编码显示 */
	private RadioButton hexRb;
	/** 是否显示时间 */
	private CheckBox showTimeCb;
	/** 是否显示颜色标记 */
	private CheckBox showColorCb;
	/** 是否自动回发 */
	private CheckBox autoSendBackCb;
	/** 扫描超时时间下拉框 */
//	private Spinner scanTimeoutSpinner;
	/** 是否自动重连下拉框 */
	private CheckBox autoReconnectCb;
	/** 尝试重连时间间隔下拉框 */
//	private Spinner reconnectIntervalSpinner;
	/** 超时重连机制持续的最长时间下拉框 */
//	private Spinner reconnectMaxTimeSpinner;
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
//	private TextView rxTv;
	/** 传送(上行流量) */
//	private TextView txTv;

	/** 消息文本框更新管理器 */
	private ViewUpdateManager msgTvUpdateManager;
	/** 接收(下行流量)文本框更新管理器 */
	private ViewUpdateManager rxTvUpdateManager;
	/** 传送(上行流量)文本框更新管理器 */
	private ViewUpdateManager txTvUpdateManager;

    /** 扫描超时时间下拉框管理器 */
    private IntSpinnerManager scanTimeoutSpinnerManager;
    /** 尝试重连时间间隔下拉框管理器 */
    private IntSpinnerManager reconnectIntervalSpinnerManager;
    /** 超时重连机制持续的最长时间下拉框管理器 */
    private IntSpinnerManager reconnectMaxTimeSpinnerManager;

	private Activity activity = this;
	/** 以Utf8编码显示还是以Hex格式显示 */
	private boolean isUtf8;
	/** 是否显示接收时间 */
	private boolean isShowTime;
	/** 是否显示颜色标记 */
	private boolean isShowColor;
	/** 是否自动回发 */
	private boolean isAutoSendBack;
	/** 扫描超时时间(单位:毫秒) */
	private long scanTimeout;
	/** 是否自动重连 */
	private boolean isAutoReconnect;
	/** 重连间隔(隔几毫秒重连一次) */
	private long reconnectInterval;
	/** 重连机制持续的最长时间(单位:毫秒) */
	private long reconnectMaxTime;
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
	/** 接收(下行流量)字节数 */
	private int rxBytesCount;
	/** 接收(上行流量)字节数 */
	private int txBytesCount;
	/** 预期接收(上行流量)字节数 */
	private int txTotalBytesCount;
	/** 数据存储 */
	private PrefUtils myPref;
	/** 发送的历史记录 */
	private MsgSendHistoryList sendHistoryList;
	/** 消息数据 */
	private MsgList msgList;
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
			LogLevel logLevel = result ? LogLevel.NORMAL : LogLevel.ERROR;
			addLog(logLevel, getResources().getString(strId) + " ( " + serviceUuid + " )");
        }

        @Override
        public void onGetReadCharacteristic(boolean result) {
            int strId = result ? R.string.succ_read_characteristic_found : R.string.err_read_characteristic_not_found;
			LogLevel logLevel = result ? LogLevel.NORMAL : LogLevel.ERROR;
            addLog(logLevel, getResources().getString(strId) + " ( " + readUuid + " )");
        }

        @Override
        public void onGetWriteCharacteristic(boolean result) {
            int strId = result ? R.string.succ_write_characteristic_found : R.string.err_write_characteristic_not_found;
			LogLevel logLevel = result ? LogLevel.NORMAL : LogLevel.ERROR;
            addLog(logLevel, getResources().getString(strId) + " ( " + writeUuid + " )");
        }

		@Override
		public void onDisconnect(boolean isManual) {
			addLog(LogLevel.NORMAL, getResources().getString(isManual ? R.string.active : R.string.passive) + getResources().getString(R.string.disconnect));
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
			EncodingType encodingType = isUtf8 ? EncodingType.UTF8 : EncodingType.HEX;
			MsgModel msgModel = new MsgSendRecvModel(activity, Direction.RECEIVED, encodingType, value);
			addText(msgModel);
			rxBytesCount += value.length;
			rxTvUpdateManager.update();
			if (isAutoSendBack) {
				sendMsg(encodingType, value);
			}
		}

		@Override
		public void onWriteReturn(boolean result, String errMsg, int preparedSendBytesCount, int sendSuccBytesCount) {
			if (result) {
				addLog(LogLevel.NORMAL, R.string.send_succ);
			} else {
				if (errMsg == null) {
					addLog(LogLevel.ERROR, R.string.send_fail);
				} else {
					addLog(LogLevel.ERROR, getResources().getString(R.string.send_fail) + " : " + errMsg);
				}
			}
			txTotalBytesCount += preparedSendBytesCount;
			txBytesCount += sendSuccBytesCount;
			txTvUpdateManager.update();
		}

		@Override
		public void onReconnectStart() {
			addLog(LogLevel.NORMAL, R.string.reconnect_start);
		}

		@Override
		public void onReconnecting() {
			addLog(LogLevel.NORMAL, R.string.reconnecting);
		}

		@Override
		public void onReconnectEnd(boolean isReachReconnectMaxTime) {
			if(isReachReconnectMaxTime) {
				addLog(LogLevel.NORMAL, R.string.reconnect_reach_max_time_stop);
			} else {
				addLog(LogLevel.NORMAL, R.string.reconnect_succeed_stop);
			}
		}

		@Override
		public void onReconnectStateChange(boolean isConnected, String errMsg) {
			if (isConnected) {
				addLog(LogLevel.NORMAL, R.string.reconnect_succ);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						sendSettingBtn.setEnabled(false);
					}
				});
			} else {
				if (errMsg == null) {
					addLog(LogLevel.ERROR, R.string.reconnect_fail);
				} else {
					addLog(LogLevel.ERROR, getResources().getString(R.string.reconnect_fail) + " : " + errMsg);
				}
			}
		}

		@Override
		public void log(String log) {
			addLog(LogLevel.NORMAL, log);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		findView();
		initData();
		setData();
		setListener();
	}

	private void findView() {
//		msgSv = (ScrollView) findViewById(R.id.msg_sv);
//		msgTv = (TextView) findViewById(R.id.msg_tv);
		encodingRg = (RadioGroup) findViewById(R.id.encoding_rg);
		utf8Rb = (RadioButton) findViewById(R.id.utf8_rb);
		hexRb = (RadioButton) findViewById(R.id.hex_rb);
		showTimeCb = (CheckBox) findViewById(R.id.show_time_cb);
		showColorCb = (CheckBox) findViewById(R.id.show_color_cb);
//		scanTimeoutSpinner = (Spinner) findViewById(R.id.scan_timeout_spinner);
		autoSendBackCb = (CheckBox) findViewById(R.id.send_back_cb);
		autoReconnectCb = (CheckBox) findViewById(R.id.auto_reconnect_cb);
//		reconnectIntervalSpinner = (Spinner) findViewById(R.id.reconnect_interval_spinner);
//		reconnectMaxTimeSpinner = (Spinner) findViewById(R.id.reconnect_max_time_spanner);
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
//		rxTv = (TextView) findViewById(R.id.rx_tv);
//		txTv = (TextView) findViewById(R.id.tx_tv);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		myPref = PrefUtils.getInstance(activity);
		isUtf8 = myPref.getBoolean(Constants.SP_KEY_IS_UTF8);
		isAutoSendBack = myPref.getBoolean(Constants.SP_KEY_IS_AUTO_SEND_BACK, true);
		scanTimeout = myPref.getLong(Constants.SP_KEY_SCAN_TIMEOUT);
		isAutoReconnect = myPref.getBoolean(Constants.SP_KEY_IS_AUTO_RECONNECT);
		reconnectInterval = myPref.getLong(Constants.SP_KEY_RECONNECT_INTERVAL);
		reconnectMaxTime = myPref.getLong(Constants.SP_KEY_RECONNECT_MAX_TIME);
		isShowTime = myPref.getBoolean(Constants.SP_KEY_IS_SHOW_TIME);
		isShowColor = myPref.getBoolean(Constants.SP_KEY_IS_SHOW_COLOR);
		isSendUtf8 = myPref.getBoolean(Constants.SP_KEY_IS_SEND_UTF8);
		serviceUuid = myPref.getString(Constants.SP_KEY_UUID_SERVICE);
		if (serviceUuid == null) {
			serviceUuid = Constants.UUID_SERVICE;
		}
		readUuid = myPref.getString(Constants.SP_KEY_UUID_READ);
		if (readUuid == null) {
			readUuid = Constants.UUID_CHARACTERISTIC_READ;
		}
		writeUuid = myPref.getString(Constants.SP_KEY_UUID_WRITE);
		if (writeUuid == null) {
			writeUuid = Constants.UUID_CHARACTERISTIC_WRITE;
		}
		isDetectUuidWhenConnect = myPref.getBoolean(Constants.SP_KEY_IS_DETECT_UUID_WHEN_CONNECT);
		String sendHistoryStr = myPref.getString(Constants.SP_KEY_SEND_HISTORY);

		msgTvUpdateManager = new ViewUpdateManager(activity, R.id.msg_tv, this);
		rxTvUpdateManager = new ViewUpdateManager(activity, R.id.rx_tv, this);
		txTvUpdateManager = new ViewUpdateManager(activity, R.id.tx_tv, this);

		scanTimeoutSpinnerManager = new IntSpinnerManager(activity, R.id.scan_timeout_spinner);
		scanTimeout = scanTimeoutSpinnerManager.setSelectionByValue((int) (scanTimeout / 1000)) * 1000L;

		reconnectIntervalSpinnerManager = new IntSpinnerManager(activity, R.id.reconnect_interval_spinner);
		reconnectInterval = reconnectIntervalSpinnerManager.setSelectionByValue((int) (reconnectInterval / 1000)) * 1000L;

		reconnectMaxTimeSpinnerManager = new IntSpinnerManager(activity, R.id.reconnect_max_time_spanner);
		adjustReconnectMaxTimeSpinnerItem();

		sendHistoryList = new MsgSendHistoryList(sendHistoryStr);
		msgList = new MsgList();
		bleDeviceList = new ArrayList<BleDeviceModel>();
		bleScanFilter = new BleScanFilter(activity, null);
		bleConnector = new BleConnector(activity, bleCallback, isAutoReconnect, reconnectInterval, reconnectMaxTime);
	}

	@Override
	public void updateView(View view) {
		TextView textView = (TextView) view;
		switch (view.getId()) {
			case R.id.msg_tv:
				resetText(textView);
				break;
			case R.id.rx_tv:
				resetRx(textView);
				break;
			case R.id.tx_tv:
				resetTx(textView);
				break;
		}
	}

	/**
	 * 将数据绑定到控件上
	 */
	private void setData() {
		utf8Rb.setChecked(isUtf8);
		hexRb.setChecked(!isUtf8);
		showTimeCb.setChecked(isShowTime);
		showColorCb.setChecked(isShowColor);
		autoSendBackCb.setChecked(isAutoSendBack);
		autoReconnectCb.setChecked(isAutoReconnect);
        scanTimeoutSpinnerManager.setSelectionByValue((int)(scanTimeout / 1000));
		reconnectIntervalSpinnerManager.setSelectionByValue((int)(reconnectInterval / 1000));
        reconnectMaxTimeSpinnerManager.setSelectionByValue((int)(reconnectMaxTime / 1000));
		neverSelectUuidWhenConnectCb.setChecked(!isDetectUuidWhenConnect);
		deleteDevice();
		clearText();
	}

	@Override
	protected void onStop() {
		try {
			myPref.putString(Constants.SP_KEY_SEND_HISTORY, sendHistoryList.toSerializeStr());
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onStop();
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
				myPref.putBoolean(Constants.SP_KEY_IS_UTF8, isUtf8);
			}
		});
		showTimeCb.setOnCheckedChangeListener(this);
		showColorCb.setOnCheckedChangeListener(this);
		autoSendBackCb.setOnCheckedChangeListener(this);
		autoReconnectCb.setOnCheckedChangeListener(this);
		neverSelectUuidWhenConnectCb.setOnCheckedChangeListener(this);
		scanTimeoutSpinnerManager.setOnItemSelectedListener(this);
        reconnectIntervalSpinnerManager.setOnItemSelectedListener(this);
        reconnectMaxTimeSpinnerManager.setOnItemSelectedListener(this);
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
		switch(adapterView.getId()) {
			case R.id.scan_timeout_spinner:
				scanTimeout = scanTimeoutSpinnerManager.getSpinnerIntValue(position) * 1000L;
                myPref.putLong(Constants.SP_KEY_SCAN_TIMEOUT, scanTimeout);
				break;
			case R.id.reconnect_interval_spinner:
				reconnectInterval = reconnectIntervalSpinnerManager.getSpinnerIntValue(position) * 1000L;
                myPref.putLong(Constants.SP_KEY_RECONNECT_INTERVAL, reconnectInterval);
				adjustReconnectMaxTimeSpinnerItem();
				bleConnector.setReconnectInterval(reconnectInterval);
				break;
			case R.id.reconnect_max_time_spanner:
				reconnectMaxTime = reconnectMaxTimeSpinnerManager.getSpinnerIntValue(position) * 1000L;
                myPref.putLong(Constants.SP_KEY_RECONNECT_MAX_TIME, reconnectMaxTime);
				bleConnector.setReconnectMaxTime(reconnectMaxTime);
				break;
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
				key = Constants.SP_KEY_IS_SHOW_TIME;
				msgTvUpdateManager.update();
				break;
			case R.id.show_color_cb:
				isShowColor = paramBoolean;
				key = Constants.SP_KEY_IS_SHOW_COLOR;
				msgTvUpdateManager.update();
				break;
			case R.id.send_back_cb:
				isAutoSendBack = paramBoolean;
				key = Constants.SP_KEY_IS_AUTO_SEND_BACK;
				break;
			case R.id.auto_reconnect_cb:
				isAutoReconnect = paramBoolean;
				key = Constants.SP_KEY_IS_AUTO_RECONNECT;
				bleConnector.setAutoReconnect(isAutoReconnect);
				setReconnectOptionEnabled(isAutoReconnect);
				break;
			case R.id.never_select_uuid_cb:
				isDetectUuidWhenConnect = !paramBoolean;
				//由于这个是相反的因此不能进入常规设置项
				myPref.putBoolean(Constants.SP_KEY_IS_DETECT_UUID_WHEN_CONNECT, isDetectUuidWhenConnect);
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
				addLog(LogLevel.NORMAL, R.string.delete_device);
				deleteDevice();
				break;
			case R.id.clear_text_btn:
				clearText();
				break;
		}
	}

	/**
	 * 分别设置"扫描设备"、"停止搜索"、"删除设备"、"发送设置", 4个按钮的启用状态
	 *
	 * @param scanBtnEnabled
	 *            是否启用"扫描设备"按钮
	 * @param stopBtnEnabled
	 *            是否启用"停止搜索"按钮
	 * @param deleteBtnEnabled
	 *            是否启用"删除设备"按钮
	 * @param sendBtnEnabled
	 * 			  是否启用"发送设置"和"uuid选择"按钮
	 */
	private void setBleBtnEnabled(final boolean scanBtnEnabled, final boolean stopBtnEnabled, final boolean deleteBtnEnabled, final boolean sendBtnEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				scanDeviceBtn.setEnabled(scanBtnEnabled);
				stopScanBtn.setEnabled(stopBtnEnabled);
				deleteDeviceBtn.setEnabled(deleteBtnEnabled);
				sendSettingBtn.setEnabled(sendBtnEnabled);
				uuidSelectBtn.setEnabled(sendBtnEnabled);
			}
		});
	}

	/**
	 * 设置是否启用重连参数配置控件
	 */
	private void setReconnectOptionEnabled(boolean enabled) {
		reconnectIntervalSpinnerManager.setEnabled(enabled);
		reconnectMaxTimeSpinnerManager.setEnabled(enabled);
	}

	/**
	 * 当"重连间隔时间"改变时，需要调整"重连有效时长"的项，使得其每一个项的值都大于当前选中的"重连间隔时间"。
	 * 同时调整"重连有效时长"的选中项到新的索引上。
	 * 之后重新设置"重连有效时长"为该下拉框中有的且最接近的项的值
	 */
	private void adjustReconnectMaxTimeSpinnerItem() {
		reconnectMaxTimeSpinnerManager.setSpinnerIntValueNotLessThan(R.array.reconnect_max_time_arr, (int) (reconnectInterval / 1000));
		reconnectMaxTime = reconnectMaxTimeSpinnerManager.setSelectionByValue((int) (reconnectMaxTime / 1000)) * 1000L;
		myPref.putLong(Constants.SP_KEY_RECONNECT_MAX_TIME, reconnectMaxTime);
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
				myPref.putBoolean(Constants.SP_KEY_IS_SEND_UTF8, isSendUtf8);
			}
		});
		new AlertDialog.Builder(activity).setTitle(R.string.send_setting).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int which) {
				String msg = sendMsgEt.getText().toString();
				byte[] value = null;
				if (isSendUtf8) {
					try {
						value = msg.trim().getBytes(Constants.CHARSET_NAME);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						addLog(LogLevel.ERROR, e.getMessage());
					}
				} else {
					try {
						value = HexConvertUtils.hexStrToByteArr(activity, msg);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						addLog(LogLevel.ERROR, e.getMessage());
					}
				}
				if (value != null) {
					EncodingType encodingType = isSendUtf8 ? EncodingType.UTF8 : EncodingType.HEX;
					sendMsg(encodingType, value);
				}
			}
		}).setNegativeButton(R.string.cancel, null).show();
	}

    /**
     * 正确的uuid格式
     * 00000000-0000-0000-0000-000000000000
     *
     * @return uuid字符串是否合法
     */
    public static boolean verifyUuid(String uuid) {
        if (uuid.length() == 36) {
            Pattern pattern = Pattern.compile("^\\d{8}-\\d{4}-\\d{4}-\\d{4}-\\d{12}$");
            return pattern.matcher(uuid).matches();
        }
        return false;
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
		new AlertDialog.Builder(activity).setTitle(R.string.setting_uuid).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int which) {
				String tmpServiceUuid = serviceUuidEt.getText().toString();
				String tmpReadUuid = readUuidEt.getText().toString();
				String tmpWriteUuid = writeUuidEt.getText().toString();
				LogLevel logLevel = LogLevel.ERROR;
				String log = "";
				if (!verifyUuid(tmpServiceUuid)) {
					log = getResources().getString(R.string.err_uuid_format_service);
				}
				if (!verifyUuid(tmpReadUuid)) {
					if (!TextUtils.isEmpty(log)) {
						log += "\n";
					}
					log += getResources().getString(R.string.err_uuid_format_read);
				}
				if (!verifyUuid(tmpWriteUuid)) {
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
					logLevel = LogLevel.NORMAL;
					log = getResources().getString(R.string.set_succ);
				}
				addLog(logLevel, log);
			}
		}).setNegativeButton(R.string.cancel, null).show();
	}

	/**
	 * 存储并更新BLE连接器service的UUID
	 */
	private void setServiceUuid(String uuid) {
		serviceUuid = uuid;
		myPref.putString(Constants.SP_KEY_UUID_SERVICE, serviceUuid);
		bleConnector.setServiceUuid(uuid);
	}

	/**
	 * 存储并更新BLE连接器ReadCharacteristic的UUID
	 */
	private void setReadUuid(String uuid) {
		readUuid = uuid;
		myPref.putString(Constants.SP_KEY_UUID_READ, readUuid);
		bleConnector.setCharacteristicReadUuid(uuid);
	}

	/**
	 * 存储并更新BLE连接器WriteCharacteristic的UUID
	 */
	private void setWriteUuid(String uuid) {
		writeUuid = uuid;
		myPref.putString(Constants.SP_KEY_UUID_WRITE, writeUuid);
		bleConnector.setCharacteristicWriteUuid(uuid);
	}

	/**
	 * 显示"关于"对话框
	 */
	private void showAboutDialog() {
		String aboutContent;
		try {
			aboutContent = FileUtils.getAssetsFileText(activity, Constants.FILE_NAME_ABOUT);
		} catch (IOException e) {
			e.printStackTrace();
			aboutContent = getResources().getString(R.string.err_get_about);
		}
		new AlertDialog.Builder(activity).setTitle(R.string.about).setMessage(aboutContent).setPositiveButton(R.string.ok, null).show();
	}

	/**
	 * 开始查找BLE设备
	 */
	private void scanDevice() {
		bleScanFilter.setScanTimeout(scanTimeout);
		bleScanFilter.startLeScan(new IBleScanCallback() {

			@Override
			public void onBluetoothNotOpen() {
				addLog(LogLevel.ERROR, R.string.err_bluetooth_is_not_enabled);
			}

			@Override
			public void onScanStarted() {
				setBleBtnEnabled(false, true, false, false);
				bleDeviceList.clear();
				addLog(LogLevel.NORMAL, R.string.scan_device_start);
			}

			@Override
			public void onScanResult(BluetoothDevice device, int rssi) {
				BleDeviceModel model = new BleDeviceModel(activity, device, rssi);
				if (!bleDeviceList.contains(model)) {
					bleDeviceList.add(model);
					boolean isSingleLine = true;
					addLog(LogLevel.NORMAL, model.toString(isSingleLine));
				}
			}

			@Override
			public void onScanFinished() {
				addLog(LogLevel.NORMAL, R.string.scan_device_stop);
				showBleScanList();
				setBleBtnEnabled(true, true, false, false);
			}

			@Override
			public void onScanFail(int errorCode) {
				String errorStr;
				switch (errorCode) {
					case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
						return;
					case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
						break;
					case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
						return;
					case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
						break;
				}
				addLog(LogLevel.ERROR, R.string.err_scan_need_restart);
				msgTvUpdateManager.update();
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
		boolean isSingleLine = false;
		for (int i = 0; i < bleDeviceList.size(); i++) {
			bleDeviceArray[i] = bleDeviceList.get(i).toString(isSingleLine);
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new AlertDialog.Builder(activity).setTitle(R.string.scan_result).setItems(bleDeviceArray, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int position) {
						final BleDeviceModel device = bleDeviceList.get(position);
						boolean isSingleLine = true;
						final String deviceStr = device.toString(isSingleLine);
						addLog(LogLevel.NORMAL, getResources().getString(R.string.select) + " : " + deviceStr);
						addLog(LogLevel.NORMAL, R.string.try_connecting);
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
			addLog(LogLevel.NORMAL, getResources().getString(R.string.connect_succ) + " : " + deviceStr);
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
			addLog(LogLevel.ERROR, e.getMessage());
		}
	}

	/**
	 * 连上设备后，检索Gatt服务，显示Gatt服务的UUID的单选列表对话框
	 */
	private void showBleServicesDialog() {
		final String[] serviceUuids = bleConnector.getServiceUuids();
		if(serviceUuids == null) {
			addLog(LogLevel.ERROR, R.string.err_service_not_found_any);
		} else {
			new AlertDialog.Builder(activity).setTitle(R.string.select_service).setItems(serviceUuids, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					setServiceUuid(serviceUuids[i]);
//					addLog(activity.getResources().getString(R.string.select) + activity.getResources().getString(R.string.service_uuid) + " : " + serviceUuids[i]);
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
			addLog(LogLevel.ERROR, R.string.err_characteristic_not_found_any);
		} else {
			LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.dialog_setting_characteristics, null);
			ListView characteristicLv = (ListView) view.findViewById(R.id.characteristic_lv);
			final BleCharacteristicAdapter adapter = new BleCharacteristicAdapter(activity, characteristics);
			characteristicLv.setAdapter(adapter);
			String title = getResources().getString(R.string.select_read_write_characteristic) + "\n(" + getResources().getString(R.string.service_uuid) + ":" + serviceUuid + ")";
			new AlertDialog.Builder(activity).setTitle(title).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					String readUuid = adapter.getReadUuid();
					String writeUuid = adapter.getWriteUuid();
					if(readUuid != null) {
						setReadUuid(readUuid);
//						addLog(activity.getResources().getString(R.string.select) + activity.getResources().getString(R.string.read_uuid) + " : " + readUuid);
					} else {
						addLog(LogLevel.NORMAL, R.string.read_characteristic_not_select);
					}
					if(writeUuid != null) {
						setWriteUuid(writeUuid);
//						addLog(activity.getResources().getString(R.string.select) + activity.getResources().getString(R.string.write_uuid) + " : " + writeUuid);
					} else {
						addLog(LogLevel.NORMAL, R.string.write_characteristic_not_select);
					}
				}
			}).setCancelable(false).show();
		}
	}

	/**
	 * 显示"发送历史记录"对话框,让用户中选择相应选项重新发送
	 */
	private void showSendHistoryDialog() {
		String[] sendHistoryArray = sendHistoryList.toStrArray(activity);
		if (sendHistoryArray == null) {
			addLog(LogLevel.ERROR, R.string.err_send_history_is_empty);
		} else {
			// 这里有必要复制出一套副本,因为历史记录随时可能改变
			final ArrayList<MsgSendHistoryModel> sendHistoryList = (ArrayList<MsgSendHistoryModel>) this.sendHistoryList.getSendHistoryList().clone();
			new AlertDialog.Builder(activity).setTitle(R.string.send_history).setItems(sendHistoryArray, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface paramDialogInterface, int position) {
					MsgSendHistoryModel model = sendHistoryList.get(position);
					sendMsg(model);
				}
			}).show();
		}
	}

	/**
	 * 发送消息
	 */
	private void sendMsg(EncodingType encodingType, byte[] value) {
		MsgSendHistoryModel model = new MsgSendHistoryModel(encodingType, value);
		sendMsg(model);
	}

	/**
	 * 发送消息
	 */
	private void sendMsg(MsgSendHistoryModel model) {
		final byte[] value = model.getMsg();
		if (value == null || value.length == 0) {
			return;
		}
		if(!bleConnector.isConnectedAndInitSucc()) {
			addLog(LogLevel.ERROR, R.string.err_ble_connect_nothing);
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					bleConnector.write(value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
		sendHistoryList.addToSendHistory(model);
		addText(model);
	}

	/**
	 * 将"新的消息"添加到文本框中, 并记录该消息
	 */
	private void addText(MsgSendHistoryModel model) {
		MsgSendRecvModel msgModel = new MsgSendRecvModel(activity, model);
		addText(msgModel);
	}

	/**
	 * 将"日志信息"添加到文本框中
	 */
	private void addLog(LogLevel logLevel, int logStrId) {
		addLog(logLevel, getResources().getString(logStrId));
	}

	/**
	 * 将"日志信息"添加到文本框中
	 */
	private void addLog(LogLevel logLevel, String log) {
		MsgLogModel model = new MsgLogModel(logLevel, log);
		addText(model);
	}

	/**
	 * 将"新的消息"添加到文本框中
	 */
	private void addText(MsgModel model) {
		msgList.add(model);
		msgTvUpdateManager.update();
	}

	/**
	 * 由于是否显示时间这一项变了，所以要重新设置文本框文本
	 */
	private void resetText(final TextView view) {
		final String str = msgList.toString(activity, isShowTime, isShowColor);
		if(isShowColor) {
			view.setText(Html.fromHtml(str));
		} else {
			view.setText(str);
		}
	}

	/**
	 * 设置接收数据统计值
	 */
	private void resetRx(final TextView view) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				view.setText(String.format(getResources().getString(R.string.rx_description), rxBytesCount));
			}
		});
	}

	/**
	 * 设置发送数据统计值
	 */
	private void resetTx(final TextView view) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				view.setText(String.format(getResources().getString(R.string.tx_description), txBytesCount, txTotalBytesCount - txBytesCount, txTotalBytesCount));
			}
		});
	}

	/**
	 * 清空文本框
	 */
	private void clearText() {
		msgList.clear();
		rxBytesCount = 0;
		txBytesCount = 0;
		txTotalBytesCount = 0;
		msgTvUpdateManager.update();
		rxTvUpdateManager.update();
		txTvUpdateManager.update();
	}

}
