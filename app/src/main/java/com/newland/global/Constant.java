package com.newland.global;

public class Constant {

	/** 字符串编码 */
	public static final String CHARSET_NAME = "utf-8";

	/** 显示utf8还是hex编码 */
	public static final String SP_KEY_IS_UTF8 = "isUtf8";
	/** 是否需要显示时间 */
	public static final String SP_KEY_IS_SHOW_TIME = "isShowTime";
	/** 是否自动回发 */
	public static final String SP_KEY_IS_AUTO_SEND_BACK = "isSendBack";
	/** 是否需要自动重连 */
	public static final String SP_KEY_IS_AUTO_RECONN = "isAutoReconn";
	/** 重连下拉框当前选中的索引号 */
	public static final String SP_KEY_RECONN_INTERVAL_SELECTION = "reconnIntervalSelection";
	/** 扫描超时下拉框当前选中的索引号 */
	public static final String SP_KEY_SCAN_TIMEOUT_SELECTION = "scanTimeoutSelection";
	/** 上次发送时的编码使用的是utf8还是hex */
	public static final String SP_KEY_IS_SEND_UTF8 = "isSendUtf8";
	/** Ble服务UUID */
	public static final String SP_KEY_UUID_SERVICE = "serviceUUID";
	/** 读特征UUID */
	public static final String SP_KEY_UUID_READ = "readUUID";
	/** 写特征UUID */
	public static final String SP_KEY_UUID_WRITE = "writeUUID";
	/** 连接时是否检测UUID,并弹窗供用户选择 */
	public static final String SP_KEY_IS_DETECT_UUID_WHEN_CONNECT = "isDetectUuidWhenConnect";
	/** 发送历史记录 */
	public static final String SP_KEY_SEND_HISTORY = "sendHistory";

	/** "关于"文件路径 */
	public static final String FILE_NAME_ABOUT = "about.txt";

	/** 默认服务UUID */
	public static final String UUID_SERVICE = "00008FFF-1212-EFDE-1523-785FEABCD123";
	/** 默认读特征UUID */
	public static final String UUID_CHARACTERISTIC_READ = "00008FFB-1212-EFDE-1523-785FEABCD123";
	/** 默认写特征UUID */
	public static final String UUID_CHARACTERISTIC_WRITE = "00008FFA-1212-EFDE-1523-785FEABCD123";

	/** 最大保存的发送历史记录条目 */
	public static final byte MAX_SEND_HISTORY_COUNT = 10;

	/** 检测时间间隔(单位:毫秒) */
	public static final long DETECT_TIME_INTERVAL = 500;
}
