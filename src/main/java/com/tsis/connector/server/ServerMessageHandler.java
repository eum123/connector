package com.tsis.connector.server;

import org.jboss.netty.channel.Channel;

public interface ServerMessageHandler {
	
	/**
	 * 연결되었을때 발생
	 * @param channel
	 */
	public void channelOpen(Channel channel);

	/**
	 * 연결 종료 되었을때 발생
	 * @param channel
	 */
	public void channelClosed(Channel channel);
	
	
	/**
	 * 메시지를 수신 했을때 발생
	 * @param session
	 * @param message
	 */
	public void messageReceived(Channel channel, byte[] message);
	
	/**
	 * 메시지를 송신 후 발생
	 * @param session
	 * @param message
	 */
	public void messageSent(Channel channel, byte[] message);
	
	
	/**
	 * 오류 발생시 발생
	 * @param session
	 * @param cause
	 */
	public void exceptionCaught(Channel channel, Throwable cause);
	
	
}
