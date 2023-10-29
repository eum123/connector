package com.mj.connector.server.bridge;

import com.tsis.connector.server.bridge.AsyncServerBridge;
import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.MessageHandler;
import com.tsis.connector.client.async.AsyncClient;

public class AsyscServerBridgeTest {
	static {
		BasicConfigurator.configure();
	}
	
	
	private AsyncServerBridge server = null;
	
	public static void main(String[] args) {
		AsyscServerBridgeTest t = new AsyscServerBridgeTest();
		try {
			t.setUp();
			
			Thread.sleep(1000);
			
			t.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				t.tearDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void test() throws Exception {
		AsyncClient client = null;
		try {
			client = (AsyncClient)DriverManager.getClient("client:async://127.0.0.1:30001?readSize=10&disconnect=false");
			
			client.setMessageHandler(new MessageHandler() {

				public void messageReceived(byte[] message) {
					System.out.println("CLIENT RCVD : " + new String(message));
				}

				public void messageSent(byte[] message) {
					
					System.out.println("CLIENT SENT : " + new String(message));
				}

				public void exceptionCaught(Throwable cause) {
					cause.printStackTrace();
				}
				
			});
			
			client.write("0123456789".getBytes());
			
			client.write("0123456789".getBytes());
			
			client.write("0123456789".getBytes());
			
			Thread.sleep(5000);
			
		} finally {
			if(client != null) {
				client.stop();
			}
		}
	}
	
	public void setUp() throws Exception {
		
		server = new AsyncServerBridge("server:async://127.0.0.1:30001?readSize=10", "com.tsis.connector.server.bridge.MyExecutor");
		server.setWorkerCount(10);
		server.start();
		
		
	}
	public void tearDown() throws Exception{
		if(server != null) {
			server.stop();
		}
	}
}
