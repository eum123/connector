package com.tsis.connector.client.loadbalancer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.NettyClient;
import com.tsis.connector.client.pool.ConnectorPoolFactory;

/**
 * RoundRobin 방식으로 ConnectorPoolFactory에서 client를 구하는 Class
 * @author jin
 *
 */
public class RoundRobin implements LoadBalancer {
	private static final Logger log = LoggerFactory.getLogger(RoundRobin.class);
	
	//순차 추출을 위해
	private AtomicInteger index = new AtomicInteger(0);
	
	public Client extract(ConnectorPoolFactory[] factories) throws Exception {

		int length = factories.length;
		if(index.get() > length - 1) {
			index.set(0);
		}
		int localIndex = index.getAndIncrement() % length;

		//System.out.println("+*+*+*+*+*+*+*+*+*+*+*+*+*+ localIndex : " + localIndex);

		for(int i=0 ;i<length ;i++) {
			Client client = null;
			try {
				client = factories[localIndex].getConnector();
				if (client.isAvailable()) {
					if (client instanceof NettyClient && ((NettyClient) client).getConfiguration().isDisconnect() ) {
						try {
							((NettyClient) client).checkAndCreateChannel();
						} catch (Exception e) {
							factories[localIndex].release(client);
							log.error("try to connect to Server, but Exception occure.",e);
							continue;
						}
					}
					//System.out.println("OK ********************* index.get() : " + index.get());
					return client;
				} else {
					//System.out.println("NO ********************* index.get() : " + index.get());
					factories[localIndex].release(client);
				}
			} catch (Exception e) {
				log.warn("ConnectorPoolFactory({})에서 Connector를 구할 수 없음", factories[localIndex].getUri(), e);
				factories[localIndex].release(client);
			} finally {
				/////////////////////////////////////////
				localIndex++;
				if(localIndex > length - 1) {
					localIndex = 0;
				}
			}
		}
		//index.set(0);
		throw new Exception("모든 ConnectorPoolFactory에서 connector를 구할 수 없음");
	}
}
