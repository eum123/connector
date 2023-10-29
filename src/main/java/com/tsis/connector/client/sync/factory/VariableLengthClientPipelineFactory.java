package com.tsis.connector.client.sync.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.sync.handler.ClientHandler;
import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.common.GeneralProtocolEncoder;
import com.tsis.connector.common.VariableLengthProtocolDecoder;

public class VariableLengthClientPipelineFactory implements ChannelPipelineFactory {
	
	protected Logger log = LoggerFactory
			.getLogger(VariableLengthClientPipelineFactory.class);
	
	private ConnectorConfiguration configuration = null;

	public VariableLengthClientPipelineFactory(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline channelPipeline = Channels.pipeline();
		
		channelPipeline.addLast("decoder", new VariableLengthProtocolDecoder(configuration));
		
		channelPipeline.addLast("encoder",  new GeneralProtocolEncoder(configuration));
		
		channelPipeline.addLast("handler", new ClientHandler(configuration));
		
		return channelPipeline;
	}

}
