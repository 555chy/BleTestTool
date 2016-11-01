package com.newland.ble;

import java.util.HashSet;
import java.util.UUID;

/**
 * Ble扫描参数
 */
public class BleScanParams {
	/** 默认扫描超时时间 */
	private static final long DEF_SCAN_TIMEOUT = 5000;

	/** 符合条件的服务端广播的uuid */
	private HashSet<UUID> advertiseServiceUuidSet;
	/** 扫描超时时间(单位:毫秒) */
	private long scanTimeout = DEF_SCAN_TIMEOUT;

	/**
	 * 添加需要筛选的服务端广播的uuid<br>
	 * 如果不添加默认会显示所有的Ble服务器
	 * <p>
	 * <pre>
	 * uuid共48位，格式如下
	 * 00000000-0000-0000-8000-000000000000
	 * </pre>
	 *
	 * @throws IllegalArgumentException if advertiseServiceUuid is not formatted correctly.
	 */
	public void putServiceUuid(String advertiseServiceUuid) throws IllegalArgumentException {
		if (advertiseServiceUuidSet == null) {
			advertiseServiceUuidSet = new HashSet<UUID>();
		}
		advertiseServiceUuidSet.add(UUID.fromString(advertiseServiceUuid));
	}

	/**
	 * @return 返回是否存在需要进行筛选的uuid
	 */
	public boolean hasServiceUuid() {
		return (advertiseServiceUuidSet != null && advertiseServiceUuidSet.size() > 0);
	}

	/**
	 * @return 获取所有待筛选的uuid
	 */
	public UUID[] getAdvertiseServiceUuidArray() {
		if (advertiseServiceUuidSet == null || advertiseServiceUuidSet.size() == 0) {
			return null;
		}
		UUID[] uuidArray = new UUID[advertiseServiceUuidSet.size()];
		return advertiseServiceUuidSet.toArray(uuidArray);
	}

	/**
	 * 设置扫描超时时间,该超时时间必须大于0<br>
	 * 如果不设置超时时间会采用默认的超时时间{@link #DEF_SCAN_TIMEOUT}
	 *
	 * @param scanTimeout 扫描超时时间(单位:毫秒)
	 * @throws IllegalArgumentException if scanTimeout < 0
	 */
	public void setScanTimeout(long scanTimeout) throws IllegalArgumentException {
		if (scanTimeout <= 0) {
			//throw new IllegalArgumentException("scanTimeout must larger than zero");
			scanTimeout = DEF_SCAN_TIMEOUT;
		}
		this.scanTimeout = scanTimeout;
	}

	/**
	 * @return 扫描超时时间(单位:毫秒)
	 */
	public long getScanTimeout() {
		return scanTimeout;
	}
}
