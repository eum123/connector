package com.tsis.connector.client.http.handler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.http.ConnHttpResponse;
import com.tsis.connector.common.Callback;
import com.tsis.connector.common.ConnectorConfiguration;

public class HttpClientHandler extends SimpleChannelHandler {
	private static final Logger log = LoggerFactory.getLogger(HttpClientHandler.class);

	private boolean readingChunks;
	private ConnectorConfiguration configuration = null;
	//private Timer timer = null; // 수신 처리 로직 변경으로 인해 제거

	public HttpClientHandler(ConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		log.info("HTTP Message : Received: {}", e.getChannel());
		String receiveData = "";
		byte[] buffer = null;
		Map headers = null;

		if (!readingChunks) {
			HttpResponse response = (HttpResponse) e.getMessage();
			if (!response.headers().names().isEmpty()) {
				headers = new HashMap<String, String>();
                for (String name: response.headers().names()) {
                    for (String value: response.headers().getAll(name)) {
                        receiveData += name + ": " + value + "\r\n";
                        if(headers.containsKey(name)) { // 해당 헤더key가 기존에 있었는지?? (ex. Set-Cookie 가 여러번 들어있는 경우)
                        	if((headers.get(name)) instanceof String) { // 기존 값이 한 건 있었을 때
                        		List list = new LinkedList();
                        		list.add(headers.get(name));	// 기존 값 넣기
                        		list.add(value);	// 이번 값 넣기    
                        		headers.put(name, list);
                        	} else if((headers.get(name)) instanceof List) { // 여러건 있었을 때
                        		((List)headers.get(name)).add(value);
                        	} else {
                        		log.error("Not support type of header : [" + name+"]");
                        	}
                        } else { // 헤더값이 처음일 때
                        	headers.put(name, value);
                        }
                        
                    }
                }
            }
			if (response.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    buffer = new byte[content.readableBytes()];
					content.readBytes(buffer);
                } else { // Content 에 데이터가 없을 때
                	buffer = "".getBytes();
                }
            }			
		} else { // Pipeline 에 HttpAggregator 추가로 필요 없을 수 있으나, 소스는 유지함
			HttpChunk chunk = (HttpChunk) e.getMessage();
			if (chunk.isLast()) {
				readingChunks = false;

			} else {
				ChannelBuffer content = chunk.getContent();
				buffer = new byte[content.readableBytes()];
				content.readBytes(buffer);

			}
		}

		if (log.isDebugEnabled()) {
			if(buffer.length > 524288) {
				log.debug("Channel: {} received large body: LENGTH[{}] [{}]", new Object[] { e.getChannel(), buffer.length, receiveData + "\r\n" + new String(buffer,0,524288) });
			} else {
				log.debug("Channel: {} received body: [{}]", new Object[] { e.getChannel(), receiveData + "\r\n" + new String(buffer) });	
			}
			
		}

		//cancelTimer();

		// received response
		Callback callback = (Callback) ctx.getChannel().getAttachment();
		if (callback != null) {
			
			if(headers != null) {
				callback.success(new ConnHttpResponse(headers, buffer));
			} else {
				callback.success(new ConnHttpResponse(buffer));
			}
			
		} else {
			log.warn("callback is null.");
		}

//		log.info("Closing channel when complete at address: {}", ctx.getChannel());
/*
		Channel channel = ctx.getChannel();
		if (channel != null) {
			channel.close();
		}
		*/
	}

	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel open: {}", e.getChannel());
	}

	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		log.error("error.", e.getCause());

		// error
		Callback callback = (Callback) ctx.getChannel().getAttachment();
		if (callback != null) {
			callback.fail(e.getCause());
		} else {
			log.warn("callback is null.");
		}

		ctx.getChannel().close();
	}

	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("Channel closed: {}", e.getChannel());

		//cancelTimer(); //Timer 로직 제거

		Callback callback = (Callback) ctx.getChannel().getAttachment();
		if (callback != null) {
			callback.close(ctx.getChannel());
		} else {
			log.warn("callback is null.");
		}

		
	}

	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		/* Timer 로직 제거 
		// do we use request timeout?
		if (configuration.getRequestTimeout() > 0) {
			if (timer == null) {
				log.debug("Using request timeout {} millis", configuration.getRequestTimeout());
				createTimer(e.getChannel());
			}
		}
		*/
		log.info("HTTP Message : Write Complete : {}, RequestTimeout : {} ms", e.getChannel(), configuration.getRequestTimeout());
	}
	
	/*
	 *  Timer 로직 제거 : NettyHttpClient 수신 처리 로직 변경으로 인해 제거 
	 *   
	private void createTimer(final Channel channel) {  
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				channel.close();
			}

		};

		timer = new Timer();
		timer.schedule(task, configuration.getRequestTimeout());
	}

	private void cancelTimer() {
		if (timer != null) {
			log.debug("cancel timer");
			timer.cancel();
		}
	}
	*/
}
