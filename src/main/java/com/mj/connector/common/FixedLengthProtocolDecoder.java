package com.tsis.connector.common;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 전체 길이를 설정하여 그 길이 만큼 buffer에서 읽는 Decoder
 * @author manjin
 *
 */ 
public class FixedLengthProtocolDecoder extends FrameDecoder {
	private static final Logger log = LoggerFactory
			.getLogger(FixedLengthProtocolDecoder.class);
	
	private ConnectorConfiguration configuration = null;
	public FixedLengthProtocolDecoder(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	protected Object decode(ChannelHandlerContext arg0, Channel arg1,
			ChannelBuffer arg2) throws Exception {
		
		log.debug("recevied data ({}). buffer : {}", arg1, arg2);
		
		if (arg2.readableBytes() < configuration.getReadSize()) {
			return null;
		}
		
		//buffer로 부터 읽는다.
		byte[] buffer = new byte[configuration.getReadSize()];
		arg2.readBytes(buffer);
		log.info("[RCV] ({}). DATA({}) : [{}]", new Object[]{arg1,buffer.length ,new String(buffer)});
		return buffer;
	}	
}
