package com.tsis.connector.server.bridge;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyExecutor implements LogicExecutor{
	private static final Logger log = LoggerFactory.getLogger(MyExecutor.class);
	
	public void execute(Channel channel, byte[] data) throws Exception {
		log.debug("mylogic.................");
		
		channel.write(data);
	}

}
