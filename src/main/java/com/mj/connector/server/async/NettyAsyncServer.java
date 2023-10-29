package com.tsis.connector.server.async;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.server.ServerMessageHandler;
import com.tsis.connector.server.async.factory.FixedLengthAsyncServerPipelineFactory;
import com.tsis.connector.server.async.factory.VariableLengthAsyncServerPipelineFactory;

public class NettyAsyncServer implements AsyncServer {
	private static final Logger log = LoggerFactory.getLogger(NettyAsyncServer.class);

	protected ChannelPipelineFactory pipelineFactory;
	protected ChannelFactory channelFactory;

	private boolean isStart = false;

	protected ConnectorConfiguration configuration = null;
	
	private ServerBootstrap serverBootstrap = null;
	
	private Channel channel;
	private ChannelGroup allChannels = null;
	
	private ServerMessageHandler messageHandler = null;
	
	protected static Timer timer = null;
	
	public NettyAsyncServer(ConnectorConfiguration config) {
		this.configuration = config;
	}
	
	public void stop() throws Exception {
		
		if (timer != null) {
			timer.stop();
			timer = null;
		}
		
		stopServerBootstrap();

		isStart = false;
	}
	
	private void stopServerBootstrap() {
        // close all channels
        log.info("ServerBootstrap unbinding from {}:{}", configuration.getHost(), configuration.getPort());

        log.debug("Closing {} channels", allChannels.size());
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();

        // close server external resources
        if (channelFactory != null) {
            channelFactory.releaseExternalResources();
            channelFactory = null;
        }
        
    }

	public void start() throws Exception {
		
		this.allChannels = new DefaultChannelGroup(NettyAsyncServer.class.getName());
		
		if (timer == null) {
			timer = new HashedWheelTimer();
		}
		
		if(configuration.getType().equals(ConnectorConfiguration.FIXED_LENGTH_TYPE)) {
			pipelineFactory = new FixedLengthAsyncServerPipelineFactory(this);
		} else {
			pipelineFactory = new VariableLengthAsyncServerPipelineFactory(this);
		}
		
		startServerBootstrap();
	}
	
	private void startServerBootstrap() {
		Executor bossPool = Executors.newCachedThreadPool();
		Executor workerPool = Executors.newCachedThreadPool();
		channelFactory = new NioServerSocketChannelFactory(bossPool, workerPool);

        serverBootstrap = new ServerBootstrap(channelFactory);
        serverBootstrap.setOption("child.keepAlive", "true");
        serverBootstrap.setOption("child.tcpNoDelay", "true");
        serverBootstrap.setOption("reuseAddress", "true");
        serverBootstrap.setOption("child.reuseAddress", "true");
        serverBootstrap.setOption("child.connectTimeoutMillis", configuration.getConnectTimeout());
       
        log.debug("Created ServerBootstrap {} with options: {}", serverBootstrap, serverBootstrap.getOptions());

        // set the pipeline factory, which creates the pipeline for each newly created channels
        serverBootstrap.setPipelineFactory(pipelineFactory);

        log.info("ServerBootstrap binding to {}:{}", configuration.getHost(), configuration.getPort());
        channel = serverBootstrap.bind(new InetSocketAddress(configuration.getHost(), configuration.getPort()));
        // to keep track of all channels in use
//        allChannels.add(channel);
    }
	
	public ServerMessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(ServerMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	public ConnectorConfiguration getConfiguration() {
		return configuration;
	}
	
	public ChannelGroup getChannelGroup() {
		return allChannels;
	}

	public void broadcast(Object data) {
		allChannels.write(data);
	}
}
