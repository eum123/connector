package com.tsis.connector.client.http.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.tsis.connector.client.http.handler.HttpClientHandler;
import com.tsis.connector.common.ConnectorConfiguration;

public class HttpClientFactory implements ChannelPipelineFactory {

	private ConnectorConfiguration config = null;

	public HttpClientFactory(ConnectorConfiguration config) {
		this.config = config;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline objPipeline = Channels.pipeline();

		// objPipeline.addLast("encoder", new HttpClientEncoder(config));
		objPipeline.addLast("decoder", new HttpResponseDecoder());
		objPipeline.addLast("aggregator", new HttpChunkAggregator(65536));
		//objPipeline.addLast("aggregator", new HttpChunkAggregator(1048576 * 10));
		objPipeline.addLast("encoder", new EncodeHttpRequestEncoder(config));
		objPipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

		// objPipeline.addLast("decoder", new StringDecoder());
		// objPipeline.addLast("codec", new HttpClientCodec());
		// objPipeline.addLast("aggregator", new
		// HttpChunkAggregator(config.getMaxContentLength()));

		objPipeline.addLast("handler", new HttpClientHandler(config));

		return objPipeline;
	}
}
