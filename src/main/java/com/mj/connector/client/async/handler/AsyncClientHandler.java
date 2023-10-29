package com.tsis.connector.client.async.handler;

import java.util.Timer;
import java.util.TimerTask;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.async.AsyncClient;
import com.tsis.connector.common.Callback;
import com.tsis.connector.common.ConnectorConfiguration;

public class AsyncClientHandler extends SimpleChannelUpstreamHandler {
	private static final Logger log = LoggerFactory.getLogger(AsyncClientHandler.class);

	private AsyncClient client = null;
	private Timer timer = null;

	public AsyncClientHandler(AsyncClient client) {
		this.client = client;
	}

	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel open: {}", e.getChannel());
	}

	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel closed: {}", e.getChannel());
		
		cancelTimer();
		
		
		if(!client.getConfiguration().isDisconnect() && client.getConfiguration().getRequestTimeout() > 0) {
			
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent exceptionEvent)
			throws Exception {
		log.error("error.", exceptionEvent.getCause());
		
		// error
		if(client.getMessageHandler() == null) {
			log.warn("ExceptionCaught 이벤트 처리중. MessageHandler가 등록되어 있지 않음");
		} else {
			new Thread(new Runnable() {
				public void run() {
					long start = System.currentTimeMillis();
					client.getMessageHandler().exceptionCaught(exceptionEvent.getCause());
					log.debug("exceptionCaught interval : {} ms", (System.currentTimeMillis() - start));
				}
			}).start();
			
		}
		
		ctx.getChannel().close();
		
	}
	
	@Override
	public void writeComplete(
            ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		
		// do we use request timeout?
		if(!client.getConfiguration().isDisconnect() && client.getConfiguration().getRequestTimeout() > 0) {
			if (timer == null) {
				log.debug("Using request timeout {} millis", client.getConfiguration().getRequestTimeout());
				createTimer(e.getChannel());
			} else {

				log.debug("reset request timeout {} millis", client.getConfiguration().getRequestTimeout());
				
				cancelTimer();
				createTimer(e.getChannel());
			}
		}		
    }
	
	private void createTimer(final Channel channel) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				channel.close();
			}
			
		};
		
		timer = new Timer();
		timer.schedule(task, client.getConfiguration().getRequestTimeout());
	}
	private void cancelTimer() {
		if(timer != null) {
			log.debug("cancel timer");
			timer.cancel();
		}
	}

	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent messageEvent)
			throws Exception {
		
		final byte[] in = (byte[]) messageEvent.getMessage();
		
		if (log.isDebugEnabled()) {
			log.debug("Channel: {} received body: [{}]",
					new Object[] { messageEvent.getChannel(), new String(in) });
		}
		
		cancelTimer();
		
		// received response
		if(client.getMessageHandler() == null) {
			log.warn("MessageReceive 이벤트 처리중. MessageHandler가 등록되어 있지 않음");
		} else {
			if(client.getConfiguration().isResponseSync()) {
				long start = System.currentTimeMillis();
				client.getMessageHandler().messageReceived(in);
				log.debug("messageReceived interval : {} ms", (System.currentTimeMillis() - start));
			} else {
				new Thread(new Runnable() {
					public void run() {
						long start = System.currentTimeMillis();
						client.getMessageHandler().messageReceived(in);
						log.debug("messageReceived interval : {} ms", (System.currentTimeMillis() - start));
					}
				}).start();
			}
			
		}
		
		if (client.getConfiguration().isDisconnect()) {
			
			log.info("Closing channel when complete at address: {}", ctx.getChannel());
			
			Channel channel = ctx.getChannel();
			if (channel != null) {
	            channel.close();
	        }
		}

	}
}
