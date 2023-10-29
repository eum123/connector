package com.tsis.connector.server.async.handler;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tsis.connector.server.async.AsyncServer;

public class AsyncServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger log = LoggerFactory.getLogger(AsyncServerHandler.class);

	private AsyncServer server = null;

	public AsyncServerHandler(AsyncServer server) {
		this.server = server;
	}

	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel open: {}", e.getChannel());
		
		server.getChannelGroup().add(e.getChannel());
		
		if(server.getMessageHandler() == null) {
			log.warn("ChannelOpen 이벤트 처리중. MessageHandler가 등록되어 있지 않음");
		} else {
			server.getMessageHandler().channelOpen(ctx.getChannel());
		}
	}

	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel closed: {}", e.getChannel());
		
		server.getChannelGroup().remove(e.getChannel());
		
		if(server.getMessageHandler() == null) {
			log.warn("channelClosed 이벤트 처리중. MessageHandler가 등록되어 있지 않음");
		} else {
			server.getMessageHandler().channelClosed(ctx.getChannel());
		}
		
		if(!server.getConfiguration().isDisconnect() && server.getConfiguration().getRequestTimeout() > 0) {
			
		}
	}

	public void exceptionCaught(ChannelHandlerContext ctx, final ExceptionEvent exceptionEvent)
			throws Exception {
		log.error("error.", exceptionEvent.getCause());
		
		// error
		if(server.getMessageHandler() == null) {
			log.warn("ExceptionCaught 이벤트 처리중. MessageHandler가 등록되어 있지 않음");
		} else {
			new Thread(new Runnable() {
				public void run() {
					long start = System.currentTimeMillis();
					server.getMessageHandler().exceptionCaught(exceptionEvent.getChannel(), exceptionEvent.getCause());
					log.debug("exceptionCaught interval : {} ms", (System.currentTimeMillis() - start));
				}
			}).start();
			
		}
		
		ctx.getChannel().close();
		
	}
	
	@Override
	public void writeComplete(
            ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		
		if (server.getConfiguration().isDisconnect()) {
			
			log.info("Closing channel when complete at address: {}", ctx.getChannel());
			
			Channel channel = ctx.getChannel();
			if (channel != null) {
	            channel.close();
	        }
		}
    }
	
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent messageEvent)
			throws Exception {
					
		final byte[] in = (byte[]) messageEvent.getMessage();
		Channel channel = messageEvent.getChannel();
		long start = System.currentTimeMillis();
		log.debug("[1-1] Handler Start : [{}] Data({}) : [{}] , Channel: {}",new Object[]{in.hashCode(), in.length, new String(in), channel});
		/*
		if (log.isDebugEnabled()) {
			log.debug("Channel: {} received body: [{}]",
					new Object[] { messageEvent.getChannel(), new String(in) });
		}
		*/
		
		// received response
		if(server.getMessageHandler() == null) {
			log.warn("MessageReceive 이벤트 처리중. MessageHandler가 등록되어 있지 않음");
		} else {
			//long start = System.currentTimeMillis();
			server.getMessageHandler().messageReceived(messageEvent.getChannel(), in);
			//log.debug("messageReceived interval : {} ms", (System.currentTimeMillis() - start));
		}
		log.debug("[1-2] Handler End   : [{}] Data({}) : [{}] , Channel: {} - ({}) ms",new Object[]{in.hashCode(), in.length, new String(in), channel , (System.currentTimeMillis() - start)});

	}
}
