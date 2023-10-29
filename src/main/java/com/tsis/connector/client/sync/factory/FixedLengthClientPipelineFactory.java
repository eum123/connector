package com.tsis.connector.client.sync.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.sync.handler.ClientHandler;
import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.common.FixedLengthProtocolDecoder;
import com.tsis.connector.common.GeneralProtocolEncoder;

public class FixedLengthClientPipelineFactory implements ChannelPipelineFactory {
	protected Logger log = LoggerFactory
			.getLogger(FixedLengthClientPipelineFactory.class);

	private ConnectorConfiguration configuration = null;

	public FixedLengthClientPipelineFactory(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline channelPipeline = Channels.pipeline();

		channelPipeline.addLast("decoder", new FixedLengthProtocolDecoder(configuration));

		channelPipeline.addLast("encoder", new GeneralProtocolEncoder(configuration));

		channelPipeline.addLast("handler", new ClientHandler(configuration));

		return channelPipeline;
	}

}
