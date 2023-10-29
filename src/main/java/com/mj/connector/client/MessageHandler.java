package com.tsis.connector.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;

import com.tsis.connector.session.Session;

public interface MessageHandler {
	
	
	/**
	 * 메시지를 수신 했을때 발생
	 * @param session
	 * @param message
	 */
	public void messageReceived(byte[] message);
	
	/**
	 * 메시지를 송신 후 발생
	 * @param session
	 * @param message
	 */
	public void messageSent(byte[] message);
	
	
	/**
	 * 오류 발생시 발생
	 * @param session
	 * @param cause
	 */
	public void exceptionCaught(Throwable cause);
}
