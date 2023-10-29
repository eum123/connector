package com.tsis.connector.client.http;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.NettyClient;
import com.tsis.connector.client.http.factory.HttpClientFactory;
import com.tsis.connector.common.Callback;
import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.exception.CloseException;
import com.tsis.connector.exception.TimeoutException;

public class NettyHttpClient extends NettyClient implements HttpClient{
	
	private static Logger log = LoggerFactory.getLogger(NettyHttpClient.class);
	
	/**
	 * 2018-03-26 응답 처리 프로세스 수정 : Queue 제거, Timer 제거 
	 */
	private final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	ConnHttpResponse response = null;

	// Timer 와 함께 BlockingQueue 제거
	// 1. HttpChunkAggregator 사용으로 인해 Queue 사용 필요성이 없어짐
	// 2. 클라이언트 연결과 동시에 데이터 전송에 대해 서버측 연결 종료 시 queue 에 데이터 쌓이는 현상 발생
	//private BlockingQueue<ConnHttpResponse> queue = new LinkedBlockingQueue<ConnHttpResponse>(10);  

	public NettyHttpClient(ConnectorConfiguration config) {
		super(log, config);
	}

	protected ChannelPipelineFactory createPipelineFactory() {
		return new HttpClientFactory(configuration);
	}

	@Override
	protected void onStart() {

	}

	@Override
	protected void onStop() {
		
	}
	
	private HttpRequest createMessage(Map headers, byte[] data) {
		
		HttpRequest request = new DefaultHttpRequest(
                getConfiguration().getHttpVersion(),getConfiguration().getHttpType(), getConfiguration().getRawPath());
		if(headers == null || headers.size() == 0 ) {
        request.headers().set(HttpHeaders.Names.HOST, getConfiguration().getHost());
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        //request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, "gzip, deflate");
        //request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=EUC-KR");
		} else {
			Iterator eSet = headers.keySet().iterator();
			while(eSet.hasNext()) {
				String key = (String)eSet.next();
				String value = (String) headers.get(key);
				request.headers().set(key, value);
			}
		}

		if(getConfiguration().getHttpType().equals(HttpMethod.GET)) {	
			request.setUri(getConfiguration().getRawPath()+"?"+ new String(data));			
		} else {
			request.setContent(ChannelBuffers.wrappedBuffer(data));
		}

        return request;
	}

	public synchronized byte[] write(Map headers, final byte[] data) throws Exception {
		log.info("configuration.isDisconnect() : " + configuration.isDisconnect());
		lock.lock();
		
		try {
			//throw new Exception(getConfiguration().getHost() + ":" + getConfiguration().getPort() + " 로 연결되어 있지 않음");
			//connect(); // NettySyncClient 변경 사항에 따라 영향범위에 포함 -> connection() 추가함 -> connect 로직 싱크로 처리 
			checkAndCreateChannel(); 

			ClientCallback callback = new ClientCallback();
			getChannel().setAttachment(callback);
			getChannel().write(createMessage(headers, data));
	
			// 응답 처리 로직 수정
			//ConnHttpResponse response = queue.take();  
			//ConnHttpResponse response = queue.poll(configuration.getRequestTimeout(), TimeUnit.MILLISECONDS); //1. Timer 제거에 따라 take 에서 poll 로 변경
			condition.await(configuration.getRequestTimeout(), TimeUnit.MILLISECONDS);	

			if (response == null) {
				if(getChannel().isConnected()) {
					getChannel().close();
				}
				String errMsg = "TIMEOUT 응답을 수신하지 못함. response is null. channel:" + getChannelString();
				log.error(errMsg);
				throw new TimeoutException(errMsg);
			} 
			
			// 주의사항 : 메시지 수신과 연결 종료에 대한 두 값이 모두 들어있는 경우를 고려해야함 (메시지 수신이 우선)
			if (response.getMessage() != null) {	// 메시지 수신 성공
				if(headers!=null && response.getHeaders()!=null) {
					headers.clear();
					headers.putAll(response.getHeaders());
				}
				return response.getMessage();
			} else {	// 메시지 수신 실패
				if (response.getCause() != null) {
					if(response.getCause() instanceof Exception){
						throw (Exception)response.getCause();
					} else {
						throw new Exception(response.getCause());
					}
				}
				throw new Exception("메시지 수신 실패");
			}
	
			// 응답 받았거나 오류가 발생하면 Callback 에 저장되어 있음.
			/* 아래 부분은 response Cause 와 중복 되므로 삭제함 
			if (callback.isError) {
				throw new Exception(callback.cause);
			}
			*/	
		} catch (InterruptedException e) {
			throw new InterruptedException("interrupted 발생!!!. 응답을 수신하지 못함. response is null. channel:" + getChannelString());
		} finally {
			if (configuration.isDisconnect()) {
				log.info("Closing channel when complete at address: {}", getChannel());
				Channel channel = getChannel();
				if (channel != null) {
					ChannelFuture f = channel.close();
					f.awaitUninterruptibly();
				}
			}
			response = null;
			lock.unlock();
		}
	}
	
	public synchronized byte[] write(final byte[] data) throws Exception {
		return write(null,data);
	}
	

	class ClientCallback extends Callback {
		//private Throwable cause = null;
		//private boolean isError = false;
		// 처리 완료 여부
		private boolean isDone = false;
		
		private void wakeup() {
			lock.lock();
			try {
				log.debug("wake up : {}", getChannelString());
				condition.signal();
			} finally {
				lock.unlock();
			}
		}

		public void success(Object response) {
			//queue.add((ConnHttpResponse)response);
			NettyHttpClient.this.response = (ConnHttpResponse)response;
			wakeup();
		}

		public void fail(Throwable cause) {

			log.debug("ClientCallback fail. isDone({})", isDone);
			//queue.add(new ConnHttpResponse(cause));
			if(NettyHttpClient.this.response != null ) {
				NettyHttpClient.this.response.setCause(cause);
			} else {
				NettyHttpClient.this.response = new ConnHttpResponse(cause);
			}
			wakeup();
		}

		public void close(Channel channel) {
			//queue.add(new ConnHttpResponse(new Exception("connection closed : " + channel)));
			if (!configuration.isDisconnect()) {
				if(NettyHttpClient.this.response != null ) {
					NettyHttpClient.this.response.setCause(new CloseException("connection closed : " + channel));
				} else {
					NettyHttpClient.this.response = new ConnHttpResponse(new CloseException("connection closed : " + channel));	
				}
			}
			wakeup();
		}
	}
}
