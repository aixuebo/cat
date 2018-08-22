package com.dianping.cat.message.internal;

import java.util.List;

import org.unidal.helper.Splitters;

//domain-ip-时间戳-序号组成MessageId
public class MessageId {
	private static final long VERSION1_THRESHOLD = 1325347200000L; // Jan. 1 2012

	private String m_domain;

	//一个ip是4个byte组成的数字,因为数组拼接起来太长了,将其转换成小的字母/数字
	//4个byte数字可以转换成2个16进制字符,即0-9a-e,因此该ip字符串就是8个字母/数组组成的,每取2个字符就可以转换成一个byte表示的数字
	private String m_ipAddressInHex;

	private long m_timestamp;

	private int m_index;//相同时间戳下有多少个id序号

	public static MessageId parse(String messageId) {
		List<String> list = Splitters.by('-').split(messageId);
		int len = list.size();

		if (len >= 4) {
			//最后3个位置是ip、时间戳、序号
			String ipAddressInHex = list.get(len - 3);
			long timestamp = Long.parseLong(list.get(len - 2));
			int index = Integer.parseInt(list.get(len - 1));
			String domain;

			if (len > 4) { // allow domain contains '-' 说明domain中包含-
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < len - 3; i++) {//追加domain原始内容
					if (i > 0) {
						sb.append('-');
					}

					sb.append(list.get(i));
				}

				domain = sb.toString();
			} else {//domain就是第一个部分组成的
				domain = list.get(0);
			}

			return new MessageId(domain, ipAddressInHex, timestamp, index);
		}

		throw new RuntimeException("Invalid message id format: " + messageId);
	}

	MessageId(String domain, String ipAddressInHex, long timestamp, int index) {
		m_domain = domain;
		m_ipAddressInHex = ipAddressInHex;
		m_timestamp = timestamp;
		m_index = index;
	}

	public String getDomain() {
		return m_domain;
	}

	public int getIndex() {
		return m_index;
	}

	public String getIpAddress() {
		StringBuilder sb = new StringBuilder();
		String local = m_ipAddressInHex;
		int length = local.length();

		for (int i = 0; i < length; i += 2) {//每次获取两个char
			char first = local.charAt(i);//第一个char
			char next = local.charAt(i + 1);//第2个char
			int temp = 0;

			//因为是16进制的参数,而char是2个字节
			if (first >= '0' && first <= '9') {
				temp += (first - '0') << 4;
			} else {
				temp += ((first - 'a') + 10) << 4;
			}
			if (next >= '0' && next <= '9') {
				temp += next - '0';
			} else {
				temp += (next - 'a') + 10;
			}

			if (sb.length() > 0) {
				sb.append('.');
			}
			sb.append(temp);
		}

		return sb.toString();
	}

	public String getIpAddressInHex() {
		return m_ipAddressInHex;
	}

	//版本不同,表示时间戳打印的值是不同的
	public long getTimestamp() {
		if (m_timestamp > VERSION1_THRESHOLD) {
			return m_timestamp;
		} else {
			return m_timestamp * 3600 * 1000L;
		}
	}

	//获取版本号
	public int getVersion() {
		if (m_timestamp > VERSION1_THRESHOLD) {
			return 1;
		} else {
			return 2;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(m_domain);
		sb.append('-');
		sb.append(m_ipAddressInHex);
		sb.append('-');
		sb.append(m_timestamp);
		sb.append('-');
		sb.append(m_index);

		return sb.toString();
	}
}
