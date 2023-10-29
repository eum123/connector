package com.tsis.connector.client.async.factory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.async.AsyncClient;
import com.tsis.connector.client.async.handler.AsyncClientHandler;
import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.common.GeneralProtocolEncoder;
import com.tsis.connector.common.VariableLengthProtocolDecoder;

public class VariableLengthAsyncClientPipelineFactory implements ChannelPipelineFactory {
	
	protected Logger log = LoggerFactory
			.getLogger(VariableLengthAsyncClientPipelineFactory.class);
	
	private AsyncClient client = null;

	public VariableLengthAsyncClientPipelineFactory(AsyncClient client) {
		this.client = client;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline channelPipeline = Channels.pipeline();
		
		channelPipeline.addLast("decoder", new VariableLengthProtocolDecoder(client.getConfiguration()));
		
		channelPipeline.addLast("encoder",  new GeneralProtocolEncoder(client.getConfiguration()));
		
		channelPipeline.addLast("handler", new AsyncClientHandler(client));
		
		return channelPipeline;
	}

}
