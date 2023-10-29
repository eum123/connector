package com.tsis.connector.server.async.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.common.FixedLengthProtocolDecoder;
import com.tsis.connector.common.GeneralProtocolEncoder;
import com.tsis.connector.server.async.AsyncServer;
import com.tsis.connector.server.async.handler.AsyncServerHandler;

public class FixedLengthAsyncServerPipelineFactory implements ChannelPipelineFactory {
	protected Logger log = LoggerFactory
			.getLogger(FixedLengthAsyncServerPipelineFactory.class);

	private AsyncServer server = null;

	public FixedLengthAsyncServerPipelineFactory(AsyncServer server) {
		this.server = server;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline channelPipeline = Channels.pipeline();

		channelPipeline.addLast("server_decoder", new FixedLengthProtocolDecoder(server.getConfiguration()));

		channelPipeline.addLast("server_encoder", new GeneralProtocolEncoder(server.getConfiguration()));

		channelPipeline.addLast("server_handler", new AsyncServerHandler(server));

		return channelPipeline;
	}

}
