package com.tsis.connector.client.sync;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.NettyClient;
import com.tsis.connector.client.sync.factory.FixedLengthClientPipelineFactory;
import com.tsis.connector.client.sync.factory.VariableLengthClientPipelineFactory;
import com.tsis.connector.common.Callback;
import com.tsis.connector.common.ConnectorConfiguration;
import com.tsis.connector.exception.CloseException;
import com.tsis.connector.exception.ConnectException;
import com.tsis.connector.exception.TimeoutException;

/**
 * 요청하고 응답할때까지 대기
 * 
 * @author jin
 *
 */
public class NettySyncClient extends NettyClient implements SyncClient {
	private static Logger log = LoggerFactory.getLogger(NettySyncClient.class);

	private final Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	private Response response = null;
	
	public NettySyncClient(ConnectorConfiguration config) {
		super(log, config);
	}

	protected ChannelPipelineFactory createPipelineFactory() {
		if (configuration.getType().equals(
				ConnectorConfiguration.FIXED_LENGTH_TYPE)) {
			return new FixedLengthClientPipelineFactory(configuration);
		} else {
			return new VariableLengthClientPipelineFactory(configuration);
		}
	}

	@Override
	protected void onStart() {
	}

	@Override
	protected void onStop() {

	}

	public synchronized byte[] write(final byte[] data) throws Exception {
		Object obj = new Object();
		int traceNum = obj.hashCode();
		long traceTime = System.currentTimeMillis();
		log.info("[1] SyncClient Start : [{}] Data({}) : [{}]", new Object[]{traceNum, data.length, new String(data)});
		//System.out.println("******************************* WRITE !!!! ");
		lock.lock();
		
		try {
			
			// DriverManager.getClient와 write 가 빠르게 수행되는 경우 연결이 맺어 지기 전에 이 부분으로 들어오면
			// 다시 연결을 맺어 두개를 연결하게 된다. -> 수정 가능 ?? -> 수정 완료
			//if(this.channel == null ||!this.channel.isConnected()){
			checkAndCreateChannel();
			//}
			
			////  아래 부분 제거 가능한 지 확인할 것, 주의사항 : 아래 부분이 checkAndCreateChannel() 위에 있으면 안됨 
			if (getChannel() == null || !getChannel().isConnected()) {
				throw new ConnectException(getConfiguration().getHost() + ":" + getConfiguration().getPort() + " 로 연결되어 있지 않음");
			}
			
			ClientCallback callback = new ClientCallback();
			callback.setTraceNum(traceNum);
			callback.setTraceTime(traceTime);
			callback.setTraceMsg(data);
			getChannel().setAttachment(callback);

			ChannelFuture future = getChannel().write(data);

			future.addListener(new ChannelFutureListener() {

				public void operationComplete(ChannelFuture future)
						throws Exception {
					log.debug("write complete : {}", getChannelString() + " , DATA : ["+new String(data)+"]");
				}
			});
			
			// wait response
			//log.debug("****************************** CHECK 1");
			//condition.await(120 * 1000, TimeUnit.MILLISECONDS);
			condition.await(configuration.getRequestTimeout(), TimeUnit.MILLISECONDS);
			//log.debug("****************************** CHECK 2");
			if (response == null) {
				throw new TimeoutException("응답을 수신하지 못함. response is null. channel:" + getChannelString());
			}
			
			// 메시지 수신과 연결 종료에 대한 두 값이 모두 들어있는 경우를 고려하여 아래 부분 수정
			if(response.getMessage() != null) {
				byte[] res = response.getMessage();
				long curTime = System.currentTimeMillis();
				long difTime = curTime - callback.getTraceTime();
				log.info("[4] SyncClient End   : [{}] Data({}) : [{}] - ({}) ms", new Object[]{callback.getTraceNum(), res.length, new String(res), difTime});
				return res;
			} else {
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
			/*
			if (callback.isError) {
				if(callback.cause instanceof Exception){
					throw (Exception)callback.cause;
				} else {
					throw new Exception(callback.cause);
				}
			}
			*/
		} catch (InterruptedException e) {
			throw new TimeoutException(
					"interrupted 발생!!!. 응답을 수신하지 못함. response is null. channel:" + getChannelString());
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
	
	

	class ClientCallback extends Callback {
/*
		private Throwable cause = null;

		private boolean isError = false;
*/
		// 처리 완료 여부
		private boolean isDone = false;
		
		private void wakeup() {
			lock.lock();
			try {
				log.debug("wake up write() : {}", getChannelString());
				condition.signal();
			} finally {
				lock.unlock();
			}
		}

		public void success(Object message) {
			log.debug("response sucess : {}", getChannelString());
			NettySyncClient.this.response = new Response((byte[]) message);
			wakeup();
		}

		public void fail(Throwable cause) {

			log.debug("ClientCallback fail. isDone({}) : {}", isDone, getChannelString());
			if(NettySyncClient.this.response != null) {
				NettySyncClient.this.response.setCause(cause);
			} else {
				NettySyncClient.this.response = new Response(cause);	
			}
			wakeup();
		}

		public void close(Channel channel) {
			log.debug("connection close : {}", getChannelString());
			if (!configuration.isDisconnect()) {	
				if(NettySyncClient.this.response != null) {
					NettySyncClient.this.response.setCause(new CloseException("connection closed : " + channel));
				} else {
					NettySyncClient.this.response = new Response(new CloseException("connection closed : " + channel));	
				}
			}
			wakeup();
		}
	}

	class Response {
		private byte[] message;
		private Throwable cause;

		public Response(byte[] message) {
			this.message = message;
			this.cause = null;
		}

		public Response(Throwable cause) {
			this.message = null;
			this.cause = cause;
		}

		public byte[] getMessage() {
			return message;
		}

		public Throwable getCause() {
			return cause;
		}
		public void setCause(Throwable cause) {
			this.cause = cause;
		}
	}
}
