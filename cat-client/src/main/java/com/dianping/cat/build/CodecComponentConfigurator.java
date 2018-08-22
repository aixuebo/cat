package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.codec.BufferWriter;
import com.dianping.cat.message.spi.codec.EscapingBufferWriter;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

class CodecComponentConfigurator extends AbstractResourceConfigurator {

	//定义一些组件
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		//第一个参数是接口、中间的参数是实现类的name、最后一个参数是实现类
		all.add(C(BufferWriter.class, EscapingBufferWriter.ID, EscapingBufferWriter.class));//转义字符如何处理

		all.add(C(MessageCodec.class, PlainTextMessageCodec.ID, PlainTextMessageCodec.class) //
		      .req(BufferWriter.class, EscapingBufferWriter.ID));//对内容进一步转义处理

		return all;
	}
}
