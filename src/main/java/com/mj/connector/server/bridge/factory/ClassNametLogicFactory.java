package com.tsis.connector.server.bridge.factory;

import com.tsis.connector.server.bridge.LogicExecutor;

public class ClassNametLogicFactory implements LogicFactory {
	private final String className;
	
	public ClassNametLogicFactory(String className) {
		this.className = className;
	}

	public LogicExecutor newInstance() throws Exception {
		return (LogicExecutor) Class.forName(className).newInstance();
	}

}
