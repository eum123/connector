package com.tsis.connector.client.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.loadbalancer.FailOver;
import com.tsis.connector.client.loadbalancer.RoundRobin;
import com.tsis.connector.client.sync.SyncClient;

public class LoadBalancingFactoryTest_MULTI {
	static {
		BasicConfigurator.configure();
	}
	
	private LoadBalancingFactory factory = new LoadBalancingFactory();
	
	public static void main(String[] args) {
		LoadBalancingFactoryTest_MULTI t = new LoadBalancingFactoryTest_MULTI();
		try {
			t.setUp();
			
			Thread.sleep(5000);
			
			t.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				t.tearDown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void test() throws InterruptedException {
		
		Tester[] tester = new Tester[100];
		
		for(int i=0; i < 6 ; i++) {
			tester[i] = new Tester();
			tester[i].start();
		}
		Thread.sleep(100000000);

	}
	
	public void setUp() throws Exception {
		
		//server = new Server();
		//server.start();
		
		//factory.setUri("client:sync://172.70.5.125:30001?readSize=10&disconnect=false" , "client:sync://172.70.5.125:30002?readSize=10&disconnect=false");
		//factory.setUri("client:sync://172.70.5.125:30001?readSize=10&disconnect=false");

		factory.setUri("client:sync://172.70.5.126:55000?readSize=10&disconnect=false" , "client:sync://172.70.5.126:55002?readSize=10&disconnect=false");
		//factory.setUri("client:sync://172.70.5.126:55000?readSize=10&disconnect=true"  , "client:sync://172.70.5.126:55002?readSize=10&disconnect=true");
		//factory.setLoadbalancer(new RoundRobin());
		factory.setLoadbalancer(new FailOver());
		factory.setMaxActive(10);
		factory.setLazy(false);
		factory.start();
			
	}
	public void tearDown() throws Exception {
		
		factory.stop();
		

	}
	
	
	class Tester extends Thread {
		public void run() {
			for(int i = 0 ; i < 100000 ; i++) {
				SyncClient client = null;
				try {
					client = (SyncClient)factory.getConnector();
					System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG : " + client.hashCode());
					
					byte[] response = client.write(String.format("%010d", i).getBytes());
					System.out.println("RCVD : " + new String(response));
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					factory.release(client);
					/*
					try {
						sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/	
				}
			}
			
			
			
		}
		
	}
}
