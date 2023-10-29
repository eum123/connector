package com.mj.connector;

import java.net.URISyntaxException;

import org.junit.Test;

import com.tsis.connector.common.ConnectorConfiguration;

public class ConnectorConfigurationTest {
	
	@Test
	public void test() throws Exception {
		ConnectorConfiguration config = new ConnectorConfiguration("client:sync:tcp://127.0.0.1:10010?disconnect=true&requestTimeout=1000&readSize=10");
	}
}
