package com.tsis.connector.client.pool;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tsis.connector.client.loadbalancer.FailOver;
//import com.tsis.connector.client.pool.LoadBalancingFactoryTest.Server;
import com.tsis.connector.client.sync.SyncClient;

public class LoadBalancingFactoryRetryTest {
	static {
		BasicConfigurator.configure();
	}
	
	private LoadBalancingFactory factory = new LoadBalancingFactory();
	
	
	@Test
	public void test() throws Exception {
		
		SyncClient client = null;
		try {
			
			System.out.println("??????????????????//");
			client = (SyncClient)factory.getConnector();
			byte[] response = client.write("0123456789".getBytes());
			
			System.out.println("RCVD : " + new String(response));
		} finally {
			factory.release(client);
		}
	}
	
	@Before
	public void setUp() throws Exception {
		
		//factory.setUri("client:sync://127.0.0.1:30001?readSize=10&disconnect=false", "client:sync://127.0.0.1:30002?readSize=10&disconnect=false", "client:sync://127.0.0.1:30003?readSize=10&disconnect=false");
		factory.setUri("client:sync://127.0.0.1:30001?readSize=10&disconnect=false");
		factory.setLoadbalancer(new FailOver());
		factory.start();
			
	}
	@After
	public void tearDown() throws Exception {
		
		factory.stop();
		
		
	}
	
	
}
