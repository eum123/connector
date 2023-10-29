package com.tsis.connector.client.loadbalancer;

import com.tsis.connector.client.Client;
import com.tsis.connector.client.pool.ConnectorPoolFactory;

public interface LoadBalancer {
	public Client extract(ConnectorPoolFactory[] factories) throws Exception;
}
