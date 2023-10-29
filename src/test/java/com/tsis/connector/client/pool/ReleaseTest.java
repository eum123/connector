package com.tsis.connector.client.pool;



import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.loadbalancer.FailOver;
import com.tsis.connector.client.sync.SyncClient;


public class ReleaseTest {
	static {
		BasicConfigurator.configure();
	}
	public static void main(String[] args) {
		ReleaseTest t = new ReleaseTest();
		
		try {
			t.test();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void test() throws Exception {
		System.out.println("S-------");
		LoadBalancingFactory factory = new LoadBalancingFactory();
		factory.setUri("client:sync://127.0.0.1:30001?readSize=10&disconnect=false",     
		     "client:sync://127.0.0.1:30001?readSize=10&disconnect=false",    
		     "client:sync://127.0.0.1:30001?readSize=10&disconnect=false");
		factory.setLoadbalancer(new FailOver());
		
		factory.setMinEvictableIdle(0);
		factory.setLazy(true);
		
		factory.start();


		

		factory.stop();
		System.out.println("-------E");
	}
	
}