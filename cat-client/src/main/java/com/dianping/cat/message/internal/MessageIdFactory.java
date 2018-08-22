package com.dianping.cat.message.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.unidal.helper.Splitters;

import com.dianping.cat.configuration.NetworkInterfaceManager;

//为该domain在哪个ip下产生一个message计数器对象
public class MessageIdFactory {
	private volatile long m_timestamp = getTimestamp();

	private volatile AtomicInteger m_index;//同时间戳下的序号

	private String m_domain;

	private String m_ipAddress;//16进制,8个字母组成的ip地址,参见MessageId里面的描述信息

	private volatile boolean m_initialized;//是否初始化

	private MappedByteBuffer m_byteBuffer;

	private RandomAccessFile m_markFile;

	private static final long HOUR = 3600 * 1000L;

	private BlockingQueue<String> m_reusedIds = new LinkedBlockingQueue<String>(100000);

	public void close() {
		try {
			m_markFile.close();
		} catch (Exception e) {
			// ignore it
		}
	}

	private File createMarkFile(String domain) {
		File mark = new File("/data/appdatas/cat/", "cat-" + domain + ".mark");

		if (!mark.exists()) {
			boolean success = true;
			try {
				success = mark.createNewFile();
			} catch (Exception e) {
				success = false;
			}
			if (!success) {
				mark = createTempFile(domain);
			}
		} else if (!mark.canWrite()) {
			mark = createTempFile(domain);
		}
		return mark;
	}

	private File createTempFile(String domain) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File mark = new File(tmpDir, "cat-" + domain + ".mark");

		return mark;
	}

	public String getNextId() {
		String id = m_reusedIds.poll();

		if (id != null) {//从队列里面获取id
			return id;
		} else {
			long timestamp = getTimestamp();

			if (timestamp != m_timestamp) {//每一个时间戳下,index追加累加
				m_index = new AtomicInteger(0);
				m_timestamp = timestamp;
			}

			int index = m_index.getAndIncrement();

			StringBuilder sb = new StringBuilder(m_domain.length() + 32);

			sb.append(m_domain);
			sb.append('-');
			sb.append(m_ipAddress);
			sb.append('-');
			sb.append(timestamp);
			sb.append('-');
			sb.append(index);

			return sb.toString();
		}
	}

	//小时级别的时间戳
	protected long getTimestamp() {
		long timestamp = MilliSecondTimer.currentTimeMillis();

		return timestamp / HOUR; // version 2
	}

	public void initialize(String domain) throws IOException {
	    if (!m_initialized) {
		m_domain = domain;

		if (m_ipAddress == null) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();//获取ip地址
			List<String> items = Splitters.by(".").noEmptyItem().split(ip);
			byte[] bytes = new byte[4];

			for (int i = 0; i < 4; i++) {
				bytes[i] = (byte) Integer.parseInt(items.get(i));//一个字节表示的是一个整数,但是可以转换成byte存储
			}

			StringBuilder sb = new StringBuilder(bytes.length / 2);

			for (byte b : bytes) {//每一个byte需要8位,转换成16进制,需要前4个转换一次，后4个又转换一次
				sb.append(Integer.toHexString((b >> 4) & 0x0F));
				sb.append(Integer.toHexString(b & 0x0F));
			}

			m_ipAddress = sb.toString();
		}
		File mark = createMarkFile(domain);

		m_markFile = new RandomAccessFile(mark, "rw");
		m_byteBuffer = m_markFile.getChannel().map(MapMode.READ_WRITE, 0, 20);//读取文件

		if (m_byteBuffer.limit() > 0) {
			int index = m_byteBuffer.getInt();//读取序号
			long lastTimestamp = m_byteBuffer.getLong();//读取时间戳

			if (lastTimestamp == m_timestamp) { // for same hour 相同的小时
				m_index = new AtomicInteger(index + 10000);
			} else {
				m_index = new AtomicInteger(0);
			}
		}
		
		m_initialized = true;
	    }
	    
	    saveMark();
	}

	protected void resetIndex() {
		m_index.set(0);
	}

	//id重复使用,重新添加到队列
	public void reuse(String id) {
		m_reusedIds.offer(id);
	}

	//保存时间戳以及该时间戳下的序号
	public void saveMark() {
		if (m_initialized) {
			try {
				m_byteBuffer.rewind();//保存到文件中
				m_byteBuffer.putInt(m_index.get());
				m_byteBuffer.putLong(m_timestamp);
			} catch (Exception e) {
				// ignore it
			}
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}
}
