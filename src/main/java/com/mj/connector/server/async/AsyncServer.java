package com.tsis.connector.server.async;

import org.jboss.netty.channel.group.ChannelGroup;

import com.tsis.connector.server.Server;
import com.tsis.connector.server.ServerMessageHandler;

public interface AsyncServer extends Server {
	public ServerMessageHandler getMessageHandler();

	public void setMessageHandler(ServerMessageHandler messageHandler);
	
	public void broadcast(Object data);
	
	public ChannelGroup getChannelGroup();
}
