package com.tsis.connector.client.http.factory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.tsis.connector.common.ConnectorConfiguration;

public class EncodeHttpRequestEncoder extends HttpMessageEncoder{
	private String encode = "utf-8";
	private ConnectorConfiguration config;
	

	public EncodeHttpRequestEncoder(ConnectorConfiguration config) {
		this.config = config;
		encode = config.getUriEncoding();
	}
	protected void encodeInitialLine(ChannelBuffer buf, HttpMessage message)
			throws Exception {
		HttpRequest request = (HttpRequest) message;
		buf.writeBytes(request.getMethod().toString().getBytes("ASCII"));
		buf.writeByte(32);

		String uri = request.getUri();
		int start = uri.indexOf("://");
		if (start != -1) {
			int startIndex = start + 3;
			if (uri.lastIndexOf(47) <= startIndex) {
				uri = uri + '/';
			}
		}

		buf.writeBytes(uri.getBytes(encode));
		buf.writeByte(32);
		buf.writeBytes(request.getProtocolVersion().toString()
				.getBytes("ASCII"));
		buf.writeByte(13);
		buf.writeByte(10);
	}
	
	public String getEncode() {
		return encode;
	}
	public void setEncode(String encode) {
		this.encode = encode;
	}

}
