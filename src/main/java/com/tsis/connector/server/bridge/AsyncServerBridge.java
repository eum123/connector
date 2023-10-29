package com.tsis.connector.server.bridge;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.server.ListenerManager;
import com.tsis.connector.server.ServerMessageHandler;
import com.tsis.connector.server.async.AsyncServer;
import com.tsis.connector.server.bridge.factory.ClassNametLogicFactory;
import com.tsis.connector.server.bridge.factory.LogicFactory;

public class AsyncServerBridge {
	private static Logger log = LoggerFactory
			.getLogger(AsyncServerBridge.class);
	private final String url;
	private final LogicFactory logicFactory;
	private AsyncServer server = null;

	private int workerCount = 10;

	private BlockingQueue<Entry> queue;
	private Worker[] worker = null;

	public AsyncServerBridge(String url, String className) {
		this.url = url;
		logicFactory = new ClassNametLogicFactory(className);
	}

	public AsyncServerBridge(String url, String className, Logger logger) {
		this.url = url;
		logicFactory = new ClassNametLogicFactory(className);
		this.log = logger;
	}
	public AsyncServerBridge(String url, LogicFactory factory) {
		this.url = url;
		logicFactory = factory;
	}

	public AsyncServerBridge(String url, LogicFactory factory, Logger logger) {
		this.url = url;
		logicFactory = factory;
		this.log = logger;
	}

	public void start() throws Exception {
		queue = new LinkedBlockingQueue<Entry>();

		worker = new Worker[workerCount];
		for (int i = 0; i < workerCount; i++) {
			worker[i] = new Worker("bridge_worker_" + i);
			worker[i].start();
		}

		server = (AsyncServer) ListenerManager.getServer(url);

		server.setMessageHandler(new ServerMessageHandler() {

			public void messageReceived(Channel channel, byte[] message) {
				log.info("RCVD({}):{}", channel, new String(message));
				try {
					queue.put(new Entry(channel, message, System.currentTimeMillis()));
				} catch (InterruptedException e) {
					log.error("add fail", e);
				}
			}

			public void messageSent(Channel channel, byte[] message) {
				log.info("SENT({}):{}", channel, new String(message));
			}

			public void exceptionCaught(Channel channel, Throwable cause) {
				log.error("ERROR(" + channel + ")", cause);
			}

			public void channelOpen(Channel channel) {
			}

			public void channelClosed(Channel channel) {
			}

		});
	}

	public void stop() {
		try {
			if (worker != null) {
				for (int i = 0; i < workerCount; i++) {

					worker[i].terminate();
				}
			}
		} catch (Exception e) {
		}
		
		if (server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				log.error("stop fail", e);
			}
		}
	}

	class Worker extends Thread {
		private final String name;
		private boolean isStart = true;

		public Worker(String name) {
			this.name = name;
		}

		public void run() {
			while (isStart) {
				Entry entry = null;
				try {
					entry = queue.take();

					if (entry == null) {
						continue;
					}					
					LogicExecutor executor = logicFactory.newInstance();

					Channel channel = entry.getChannel();
					long start = System.currentTimeMillis();	
					log.debug("[2-1] Executor Start : [{}] Data({}) : [{}] - ({}) ms", new Object[]{entry.getData().hashCode(), entry.getData().length, new String(entry.getData()), start - entry.time});
					
					executor.execute(channel, entry.getData());
					
					long end = System.currentTimeMillis();	
					long difTime2 = end - start;
					log.debug("[2-2] Executor End   : [{}] Data({}) : [{}] - ({}) ms", new Object[]{entry.getData().hashCode(), entry.getData().length, new String(entry.getData()), difTime2});
					//log.info("{} elapse time : {}ms", name,	(System.currentTimeMillis() - start));
				} catch (InterruptedException e) {
					// ??
				} catch (Throwable e) {
					log.error("execute fail " + name + " : " + entry , e );
				}
			}
		}

		public void terminate() {
			isStart = false;
			this.interrupt();
		}		
	}

	class Entry {
		private final Channel channel;
		private final byte[] data;
		private final long time;

		public Entry(Channel channel, byte[] data , long time) {
			this.channel = channel;
			this.data = data;
			this.time = time;
		}

		public Channel getChannel() {
			return channel;
		}
		public byte[] getData() {
			return data;
		}
		public long getTime() {
			return time;
		}
		public String toString() {
			return "channel(" + channel + ") data(" + new String(data) + ")";
		}

	}

	public int getQueueSize() {
		return queue.size();
	}

	public int getWorkerCount() {
		return workerCount;
	}

	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}

}
