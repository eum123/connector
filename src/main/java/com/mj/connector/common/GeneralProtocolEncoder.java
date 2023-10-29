package com.tsis.connector.common;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MTEventMessage에 있는 byte[]를 전달한다.
 * @author manjin
 *
 */
public class GeneralProtocolEncoder extends OneToOneEncoder {
	
	protected Logger log = LoggerFactory.getLogger(GeneralProtocolEncoder.class);
	
	private ConnectorConfiguration configuration = null;
	public GeneralProtocolEncoder(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public Object encode(ChannelHandlerContext ctx, Channel ch, Object msg)
			throws Exception {
		//log.info("send data ({}). buffer : [{}]", new Object[]{ch, new String((byte[])msg)});
		log.info("[SNT] ({}). DATA({}) : [{}]", new Object[]{ch,((byte[])msg).length ,new String((byte[])msg)});
		return ChannelBuffers.wrappedBuffer((byte[]) msg);
	} 
}
