package com.tsis.connector.client.async;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.MessageHandler;
import com.tsis.connector.client.NettyClient;
import com.tsis.connector.client.async.factory.FixedLengthAsyncClientPipelineFactory;
import com.tsis.connector.client.async.factory.VariableLengthAsyncClientPipelineFactory;
import com.tsis.connector.common.ConnectorConfiguration;

public class NettyAsyncClient extends NettyClient implements AsyncClient {
	private static Logger log = LoggerFactory.getLogger(NettyAsyncClient.class);
	
	private final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition(); 
	private MessageHandler messageHandler = null;
	
	public NettyAsyncClient(ConnectorConfiguration config) {
		super(log, config);
	}
	
	
	protected ChannelPipelineFactory createPipelineFactory() {
		if(configuration.getType().equals(ConnectorConfiguration.FIXED_LENGTH_TYPE)) {
			return new FixedLengthAsyncClientPipelineFactory(this);
		} else {
			return new VariableLengthAsyncClientPipelineFactory(this);
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onStop() {
		log.warn("Client STOP");
	}

	public void write(final byte[] data) throws Exception {
		
		checkAndCreateChannel();
		
		////  제거 예정 ??
		if (getChannel() == null || !getChannel().isConnected()) {
			throw new Exception(getConfiguration().getHost() + ":" + getConfiguration().getPort()
					+ " 로 연결되어 있지 않음");
		}
		////////////////
		
		ChannelFuture future = getChannel().write(data);
		
		future.addListener(new ChannelFutureListener(){

			public void operationComplete(ChannelFuture future) throws Exception {
				lock.lock();
				try {
					condition.signalAll();
				} finally {
					lock.unlock();
				}
				
				if (future.isSuccess()) {
					if(messageHandler != null) {
						new Thread(new Runnable() {
							public void run() {
								long start = System.currentTimeMillis();
								messageHandler.messageSent(data);
								log.debug("messageSent interval : {} ms", (System.currentTimeMillis() - start));
							}
						}).start();
						
					} else {
						log.warn("MessageSend이벤트 처리중 MessageHandler가 등록되어 있지 않음");
					}
				} else {
					log.warn("operationComplete isSuccess false");
				}
			}
			
		});
		lock.lock();
		try {
			condition.await(5,TimeUnit.SECONDS);
		} finally {
			lock.unlock();
		}
	}

	


	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

}
