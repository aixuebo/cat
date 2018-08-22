package com.dianping.cat.configuration;

import org.unidal.helper.Inets;

//获取本地的ip和host
public enum NetworkInterfaceManager {
	INSTANCE;

	private NetworkInterfaceManager() {
	}

	public String getLocalHostAddress() {
		return Inets.IP4.getLocalHostAddress();
	}

	public String getLocalHostName() {
		return Inets.IP4.getLocalHostName();
	}
}
