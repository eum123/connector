package com.tsis.connector.common;

import org.jboss.netty.channel.Channel;



public abstract class Callback {
	
	private int traceNum = 0;
	private long traceTime = 0;
	private byte[] traceMsg;
	/**
	 * 
	 * @param message
	 */
	public abstract void success(Object message);
	
	public abstract void fail(Throwable cause);
	
	public abstract void close(Channel channel);
	
	/******/
	public int getTraceNum() {
		return traceNum;
	}
	public void setTraceNum(int traceNum) {
		this.traceNum = traceNum;
	}
	public long getTraceTime() {
		return traceTime;
	}
	public void setTraceTime(long traceTime) {
		this.traceTime = traceTime;
	}
	public byte[] getTraceMsg() {
		return traceMsg;
	}
	public void setTraceMsg(byte[] traceMsg) {
		this.traceMsg = traceMsg;
	}
	
}
