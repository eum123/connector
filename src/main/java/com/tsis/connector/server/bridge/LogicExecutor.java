package com.tsis.connector.server.bridge;

import org.jboss.netty.channel.Channel;

public interface LogicExecutor {
	public void execute(Channel channel, byte[] data) throws Exception;
}
