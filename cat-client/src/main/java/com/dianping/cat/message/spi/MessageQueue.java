package com.dianping.cat.message.spi;

public interface MessageQueue {
	public boolean offer(MessageTree tree);//插入一个元素

	public boolean offer(MessageTree tree, double sampleRatio);//根据随机抽样比例插入元素到队列

	public MessageTree peek();//一撇一个元素

	public MessageTree poll();//取出一个元素

	// the current size of the queue 当前队列存储的真实size数量
	public int size();
}
