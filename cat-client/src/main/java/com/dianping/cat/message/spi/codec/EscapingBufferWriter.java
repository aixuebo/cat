package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;

//转义字符的转换 一个字节的\t 转换成字符串\t
public class EscapingBufferWriter implements BufferWriter {
	public static final String ID = "escape";

	/**
	 * 将data的内容写入到buf中
	 * @param buf
	 * @param data 其中data中可能是回车 换行等字符,因此将转换成转义字符
	 * @return
	 */
	@Override
	public int writeTo(ByteBuf buf, byte[] data) {
		int len = data.length;
		int count = len;
		int offset = 0;

		for (int i = 0; i < len; i++) {
			byte b = data[i];

			if (b == '\t' || b == '\r' || b == '\n' || b == '\\') {
				buf.writeBytes(data, offset, i - offset);
				buf.writeByte('\\');

				if (b == '\t') {
					buf.writeByte('t');
				} else if (b == '\r') {
					buf.writeByte('r');
				} else if (b == '\n') {
					buf.writeByte('n');
				} else {
					buf.writeByte(b);
				}

				count++;
				offset = i + 1;
			}
		}

		if (len > offset) {//写入最后的内容
			buf.writeBytes(data, offset, len - offset);
		}

		return count;
	}
}
