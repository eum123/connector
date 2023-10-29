package com.tsis.connector.client.sync.handler;

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

import com.tsis.connector.common.Callback;
import com.tsis.connector.common.ConnectorConfiguration;

public class ClientHandler extends SimpleChannelUpstreamHandler {
	// use NettyConsumer as logger to make it easier to read the logs as this is
	// part of the consumer
	private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private ConnectorConfiguration configuration = null;
	//private Timer timer = null;// Timer 사용하지 않으므로 삭제

	public ClientHandler(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel open: {}", e.getChannel());
	}

	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel closed: {}", e.getChannel());
		
		//cancelTimer();
		
		// close
		Callback callback = (Callback) ctx.getChannel().getAttachment();
		if (callback != null) {
			callback.close(ctx.getChannel());
		} else {
			log.warn("callback is null.");
		}
		
		if(!configuration.isDisconnect() && configuration.getRequestTimeout() > 0) {
			
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent exceptionEvent)
			throws Exception {
		log.error("error.", exceptionEvent.getCause());
		
		// error
		Callback callback = (Callback) ctx.getChannel().getAttachment();
		if (callback != null) {
			callback.fail(exceptionEvent.getCause());
		} else {
			log.warn("callback is null.");
		}
		// 이 경우는 if(ctx.getChannel().isConnected()) 의미가 없음 (연결이 끊겨져도 true 로 리턴되어 있음)
		ctx.getChannel().close();
		
	}
	
	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		Callback callback = (Callback) ctx.getChannel().getAttachment();
		long curTime = System.currentTimeMillis();
		long difTime = curTime - callback.getTraceTime();
		log.info("[2] Write Complete   : [{}] Data({}) : [{}] - ({}) ms", new Object[]{callback.getTraceNum(), callback.getTraceMsg().length, new String(callback.getTraceMsg()), difTime});
		callback.setTraceTime(curTime);
		// do we use request timeout?
		//if(!configuration.isDisconnect() && configuration.getRequestTimeout() > 0) {
		/*
		if( configuration.getRequestTimeout() > 0) {
			if (timer == null) {
				log.debug("Using request timeout {} millis", configuration.getRequestTimeout());
				createTimer(e.getChannel());
			} else {

				log.debug("reset request timeout {} millis", configuration.getRequestTimeout());
				
				cancelTimer();
				createTimer(e.getChannel());
			}
		}	
		*/	
    }
	
/*
	private void createTimer(final Channel channel) {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				log.error("request timeout "+ configuration.getRequestTimeout() +" millis. TIMEOUT !!!");
				channel.close();
			}
			
		};
		
		timer = new Timer();
		timer.schedule(task, configuration.getRequestTimeout());
	}
	private void cancelTimer() {
		if(timer != null) {
			log.debug("cancel timer");
			timer.cancel();
		}
	}
*/
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent messageEvent)
			throws Exception {
		byte[] in = (byte[]) messageEvent.getMessage();
		Callback callback = (Callback) ctx.getChannel().getAttachment();
		long curTime = System.currentTimeMillis();
		long difTime = curTime - callback.getTraceTime();
		log.info("[3] Message Received : [{}] Data({}) : [{}] - ({}) ms", new Object[]{callback.getTraceNum(), callback.getTraceMsg().length, new String(in), difTime});
		callback.setTraceTime(curTime);
		
		if (log.isDebugEnabled()) {
			log.debug("Channel: {} received body: [{}]",
					new Object[] { messageEvent.getChannel(), new String(in) });
		}
		
		//cancelTimer();
		
		// received response
		//Callback callback = (Callback) ctx.getChannel().getAttachment();
		if (callback != null) {
			callback.success(in);
		} else {
			log.warn("callback is null.");
		}
	}
}
