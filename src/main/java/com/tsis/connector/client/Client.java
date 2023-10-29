package com.tsis.connector.client;

import com.tsis.connector.common.ConnectorConfiguration;

public interface Client {
	public void start() throws Exception;
	public void stop() throws Exception;
	public boolean isAvailable() throws Exception;
	public ConnectorConfiguration getConfiguration();
}
