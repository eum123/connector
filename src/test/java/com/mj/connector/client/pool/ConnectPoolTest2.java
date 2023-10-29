package com.mj.connector.client.pool;

import com.tsis.connector.client.pool.ConnectorPoolFactory;
import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.sync.SyncClient;

public class ConnectPoolTest2 {
	static {
		BasicConfigurator.configure();
	}
	
	private ConnectorPoolFactory factory = new ConnectorPoolFactory();
	//private LoadBalancingFactory factory = new LoadBalancingFactory();
	private TestClient[] clients = null;
	public ConnectPoolTest2() {
		
	}
	
	public static void main(String[] args) throws Exception {
		ConnectPoolTest2 t = new ConnectPoolTest2();
		
		t.setUp();
		t.test();
		
		
		System.out.println("!!!!!!");
		
	}
	
	public void test() throws Exception {
		try {
		
			System.out.println("+++++++++++++++++++++++++++++++ start");
			clients = new TestClient[10];
			for(int i = 0 ; i < 10 ; i++) {
				clients[i] = new TestClient();
				
			}
			for(int i = 0 ; i < 10 ; i++) {
				System.out.println("!!!! : "+ i);
				clients[i].start();
				
			}

			Thread.sleep(50000000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				tearDown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
	}
	public void setUp() throws Exception {
		
		//server = new Server();
		//server.start();
		
		factory.setUri("client:sync://127.0.0.1:50000?readSize=10&disconnect=false");
		factory.setMaxActive(12);
		//factory.setMaxIdle(10);
		factory.start();
			
	}
	
	public void tearDown() throws Exception {
		
		factory.stop();

	}
	
	class TestClient extends Thread {
		public void run() {
			System.out.println("RUN-----------------------------------");
			SyncClient client = null;
			try {
				//Thread.sleep(10000);
				client = (SyncClient)factory.getConnector();
				System.out.println("*********** : " );
				
				byte[] response = null;
				for(int i = 0 ; i < 1 ; i++ ) {
					response = client.write("0123456789".getBytes());
				}
				System.out.println("RCVD : " + new String(response));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				factory.release(client);
			}
		}
	}
}
