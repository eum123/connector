package com.tsis.connector.common;

import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 가변길이 형태의 데이터를 읽을때 사용
 * 
 * @author manjin
 * 
 */
public class VariableLengthProtocolDecoder extends FrameDecoder {
	private static final Logger log = LoggerFactory
			.getLogger(VariableLengthProtocolDecoder.class);

	private ConnectorConfiguration configuration = null;

	public VariableLengthProtocolDecoder(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {

		log.debug("recevied data ({}). buffer : {}", new Object[] { channel, buffer });

		int start = configuration.getReadStart();
		int size = configuration.getReadSize();

		if (buffer.readableBytes() < (start + size)) {
			return null;
		}

		int length = 0;
		
		try {
			buffer.markReaderIndex();
			if (configuration.getReadEncoding().equals(
					ConnectorConfiguration.CHARACTER)) {

				byte[] lengthData = new byte[size];
				buffer.skipBytes(start);
				buffer.readBytes(lengthData);
				length = Integer.parseInt(new String(lengthData).trim())
						+ configuration.getReadHeaderLength();

			} else {
				if (configuration.getReadType().equals("java.lang.Integer")) {
					if (buffer.readableBytes() < (4 + start)) {
						// need more data
						return null;
					}

					if (configuration.getReadEndian() == ByteOrder.LITTLE_ENDIAN) {
						byte[] lengthData = new byte[4];
						buffer.skipBytes(start);
						buffer.readBytes(lengthData);
						ChannelBuffer newBuffer = ChannelBuffers.wrappedBuffer(
								configuration.getReadEndian(), lengthData);
						length = newBuffer.getInt(0)
								+ configuration.getReadHeaderLength();
					} else {
						length = buffer.getInt(start)
								+ configuration.getReadHeaderLength();
					}
				} else if (configuration.getReadType().equals("java.lang.Long")) {
					if (buffer.readableBytes() < (8 + start)) {
						// need more data
						return null;
					}

					if (configuration.getReadEndian() == ByteOrder.LITTLE_ENDIAN) {
						byte[] lengthData = new byte[8];
						buffer.skipBytes(start);
						buffer.readBytes(lengthData);
						ChannelBuffer newBuffer = ChannelBuffers.wrappedBuffer(
								configuration.getReadEndian(), lengthData);
						length = newBuffer.getInt(0)
								+ configuration.getReadHeaderLength();
					} else {
						length = (int) buffer.getLong(start)
								+ configuration.getReadHeaderLength();
					}
				} else if (configuration.getReadType().equals(
						"java.lang.String")) {
					throw new IllegalArgumentException("not support \""
							+ configuration.getReadType() + "\" type in \""
							+ configuration.getReadEncoding() + "\" encoding");
				} else {
					throw new IllegalArgumentException("unknown type : "
							+ configuration.getReadType());
				}
			}
		} finally {
			buffer.resetReaderIndex();
		}

		if (buffer.readableBytes() < length) {
			return null;
		}

		// buffer로 부터 읽는다.
		byte[] data = new byte[length];
		buffer.readBytes(data);
		log.info("[RCV] ({}). DATA({}) : [{}]", new Object[]{channel,data.length ,new String(data)});
		return data;

	}

}
