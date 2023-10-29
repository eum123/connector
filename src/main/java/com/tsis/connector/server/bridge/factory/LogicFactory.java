package com.tsis.connector.server.bridge.factory;

import com.tsis.connector.server.bridge.LogicExecutor;

public interface LogicFactory {
	public LogicExecutor newInstance() throws Exception;
}
