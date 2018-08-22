package com.dianping.cat.message.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultTransaction extends AbstractMessage implements Transaction {
	private long m_durationInMicro = -1; // must be less than 0  该事物执行周期

	private List<Message> m_children;//子小心集合

	private MessageManager m_manager;

	private boolean m_standalone;

	private long m_durationStart;

	public DefaultTransaction(String type, String name, MessageManager manager) {
		super(type, name);

		m_manager = manager;
		m_standalone = true;
		m_durationStart = System.nanoTime();
	}

	@Override
	public DefaultTransaction addChild(Message message) {
		if (m_children == null) {
			m_children = new ArrayList<Message>();
		}

		if (message != null) {
			m_children.add(message);
		} else {
			Cat.logError(new Exception("null child message"));
		}
		return this;
	}

	@Override
	public void complete() {
		try {
			if (isCompleted()) {//说明有多余一次的调用完成
				// complete() was called more than once
				DefaultEvent event = new DefaultEvent("cat", "BadInstrument");

				event.setStatus("TransactionAlreadyCompleted");
				event.complete();
				addChild(event);//但是我怀疑该事物已经被提交了，可能这次添加的事件没有意义啊
			} else {
				m_durationInMicro = (System.nanoTime() - m_durationStart) / 1000L;

				setCompleted(true);

				if (m_manager != null) {
					m_manager.end(this);
				}
			}
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public List<Message> getChildren() {
		if (m_children == null) {
			return Collections.emptyList();
		}

		return m_children;
	}

	@Override
	public long getDurationInMicros() {
		if (m_durationInMicro >= 0) {
			return m_durationInMicro;
		} else { // if it's not completed explicitly 说明没有完成,查看最后一个事件的执行时间
			long duration = 0;
			int len = m_children == null ? 0 : m_children.size();

			if (len > 0) {
				Message lastChild = m_children.get(len - 1);

				if (lastChild instanceof Transaction) {
					DefaultTransaction trx = (DefaultTransaction) lastChild;

					duration = (trx.getTimestamp() - getTimestamp()) * 1000L;
				} else {
					duration = (lastChild.getTimestamp() - getTimestamp()) * 1000L;
				}
			}

			return duration;
		}
	}

	@Override
	public long getDurationInMillis() {
		return getDurationInMicros() / 1000L;
	}

	protected MessageManager getManager() {
		return m_manager;
	}

	@Override
	public boolean hasChildren() {
		return m_children != null && m_children.size() > 0;
	}

	@Override
	public boolean isStandalone() {
		return m_standalone;
	}

	public void setDurationInMicros(long duration) {
		m_durationInMicro = duration;
	}

	public void setDurationInMillis(long duration) {
		m_durationInMicro = duration * 1000L;
	}

	public void setStandalone(boolean standalone) {
		m_standalone = standalone;
	}

	public void setDurationStart(long durationStart) {
		m_durationStart = durationStart;
	}

}
