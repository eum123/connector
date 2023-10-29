package com.tsis.connector.client.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.NettyClient;
import com.tsis.connector.client.pool.ConnectorPoolFactory;
import com.tsis.connector.exception.ConnectException;

/**
 * FailOver 방식으로 ConnectorPoolFactory에서 client를 구하는 Class
 * @author jin
 *
 */
public class FailOver implements LoadBalancer {
	private static final Logger log = LoggerFactory.getLogger(FailOver.class);

	public Client extract(ConnectorPoolFactory[] factories) throws Exception {

		int length = factories.length;

		for (int i = 0; i < length; i++) {
			Client client = null;
			try {
				client = factories[i].getConnector();
				if (client.isAvailable()) {
					if (client instanceof NettyClient && ((NettyClient) client).getConfiguration().isDisconnect() ) {
						try {
							((NettyClient) client).checkAndCreateChannel();
						} catch (Exception e) {
							factories[i].release(client);
							log.error("try to connect to Server, but Exception occure.",e);
							continue;
						}
					}
					return client;
				} else {
					factories[i].release(client);
				}
			} catch (Exception e) {
				log.warn("ConnectorPoolFactory({})에서 Connector를 구할 수 없음", factories[i].getUri(), e);
				factories[i].release(client);
			}
		}

		throw new ConnectException("모든 ConnectorPoolFactory에서 connector를 구할 수 없음");
	}

}
