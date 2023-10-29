package com.tsis.connector.client;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.exception.ConnectException;

public abstract class NettyClient {
	protected Logger log = null;

	protected ChannelPipelineFactory pipelineFactory;
	protected ChannelFactory channelFactory;

	protected boolean isStart = false;

	protected Channel channel = null;

	protected ConnectorConfiguration configuration = null;

	protected ClientBootstrap clientBootstrap = null;

	protected static Timer timer = null;
	
	protected boolean isUsage = false;
	
	private Worker reconnector = null;

	protected NettyClient(Logger logger, ConnectorConfiguration configuration) {
		if (logger == null) {
			log = LoggerFactory.getLogger(NettyClient.class);
		} else {
			log = logger;
		}

		this.configuration = configuration;
	}

	public void stop() {
		
		if(reconnector != null) {
			reconnector.terminate();
		}

		onStop();

		if (channel != null) {
			channel.close();
		}

		// close server external resources
		if (channelFactory != null) {
			channelFactory.releaseExternalResources();
			channelFactory = null;
		}

		if (timer != null) {
			timer.stop();
			timer = null;
		}

		isStart = false;
	}

	public boolean isAvailable() throws Exception {
		//System.out.println("************************** isAvailable() ");
		
		if (!configuration.isDisconnect()) { // 연결유지일때
			if (channel == null) {
				return false;
			} else {
				return channel.isConnected();
			}
		} else {	// 비연결 유지일 때
			//System.out.println("************************** CHECK 1 ");
			return true;
		}
		
	}

	public void start() throws Exception {
		if (timer == null) {
			timer = new HashedWheelTimer();
		}

		channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		//channelFactory = new OioClientSocketChannelFactory(Executors.newCachedThreadPool());
		
		pipelineFactory = createPipelineFactory();

		if(!configuration.isDisconnect()) {  // 연결 유지 방식일 경우
			reconnector = new Worker();
			reconnector.start();
		}

		onStart();
	}

	protected abstract ChannelPipelineFactory createPipelineFactory();

	protected abstract void onStart();

	protected abstract void onStop();

	public Channel getChannel() {
		return channel;
	}

	protected String getChannelString() {
		if (channel == null) {
			return "not connected";
		} else {
			return channel + " isConnected(" + channel.isConnected() + ")";
		}
	}

	public ConnectorConfiguration getConfiguration() {
		return configuration;
	}

	private void connect() throws Exception {  // connect 함수 호출 필요시 checkAndCreateChannel() 사용할 것
		ChannelFuture future = openConnection();
		this.channel = openChannel(future);
	}

	protected ChannelFuture openConnection() {
		
		ChannelFuture answer;

		if (clientBootstrap == null) {
			clientBootstrap = new ClientBootstrap(channelFactory);
			clientBootstrap.setOption("keepAlive", "true");
			clientBootstrap.setOption("tcpNoDelay", "true");
			clientBootstrap.setOption("reuseAddress", "true");
			clientBootstrap.setOption("connectTimeoutMillis", configuration.getConnectTimeout());

			// set the pipeline factory, which creates the pipeline for each
			// newly created channels
			clientBootstrap.setPipelineFactory(pipelineFactory);
		}
		
		
		answer = clientBootstrap.connect(new InetSocketAddress(configuration.getHost(),
				configuration.getPort()));

		log.debug(
				"Created new TCP client bootstrap connecting to {}:{} with options: {}",
				new Object[] { configuration.getHost(), configuration.getPort(),
						clientBootstrap.getOptions() });

		return answer;
	}

	protected Channel openChannel(ChannelFuture channelFuture) throws Exception {
		// blocking for channel to be done
		if (log.isTraceEnabled()) {
			log.trace("Waiting for operation to complete {} for {} millis", channelFuture,
					configuration.getConnectTimeout());
		}
		
		// here we need to wait it in other thread
		final CountDownLatch channelLatch = new CountDownLatch(1);
		channelFuture.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture cf) throws Exception {
				channelLatch.countDown();
			}
		});

		try {
			channelLatch.await(configuration.getConnectTimeout(), TimeUnit.MILLISECONDS);

		} catch (InterruptedException ex) {
			throw new ConnectException("Interrupted while waiting for connection to "
					+ configuration.getHost() + ":" + configuration.getPort());
		}

		if (!channelFuture.isDone() || !channelFuture.isSuccess()) {
			Exception cause = new ConnectException("Cannot connect to " + configuration.getHost() + ":"
					+ configuration.getPort());
			if (channelFuture.getCause() != null) {
				cause.initCause(channelFuture.getCause());
			}
			throw cause;
		}
		
		Channel answer = channelFuture.getChannel();

		if (log.isInfoEnabled()) {
			log.info("Creating connector to address: {}", answer.toString() );
		}
		return answer;

	}
	
	public synchronized void checkAndCreateChannel() throws Exception {
		if(channel == null || !channel.isConnected()) {
			log.debug("Channel Check : Create new channel.");
			connect();
		} 
	}
	
	class Worker extends Thread {
		private boolean isStart = true;
		public void run() {
			while(isStart) {
				try {
					checkAndCreateChannel();
				} catch (Exception e) {
					//channelFactory.releaseExternalResources();
					log.error("connect fail", e);
				} finally {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		
		public void terminate() {
			isStart = false;
			this.interrupt();
		}
	}
}
