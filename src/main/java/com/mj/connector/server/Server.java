package com.tsis.connector.server;

import com.tsis.connector.common.ConnectorConfiguration;

public interface Server {
	public ConnectorConfiguration getConfiguration();
	public void start() throws Exception;
	public void stop() throws Exception;
}
