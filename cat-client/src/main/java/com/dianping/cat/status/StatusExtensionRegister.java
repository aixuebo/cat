package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

//状态扩展注册器
public class StatusExtensionRegister {

	public static StatusExtensionRegister getInstance() {
		return s_register;
	}

	//注册的集合
	private List<StatusExtension> m_extensions = new ArrayList<StatusExtension>();

	public static StatusExtensionRegister s_register = new StatusExtensionRegister();

	private StatusExtensionRegister() {
	}

	public List<StatusExtension> getStatusExtension() {
		synchronized (this) {
			return m_extensions;
		}
	}

	public void register(StatusExtension monitor) {
		synchronized (this) {
			m_extensions.add(monitor);
		}
	}

	public void unregister(StatusExtension monitor) {
		synchronized (this) {
			m_extensions.remove(monitor);
		}
	}
}
