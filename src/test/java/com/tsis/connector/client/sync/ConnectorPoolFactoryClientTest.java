package com.tsis.connector.client.sync;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.pool.ConnectorPoolFactory;

public class ConnectorPoolFactoryClientTest {
	static {
		 BasicConfigurator.configure();
	}

	private ConnectorPoolFactory factory = new ConnectorPoolFactory();

	public static void main(String[] args) {
		ConnectorPoolFactoryClientTest t = new ConnectorPoolFactoryClientTest();

		try {
			t.setUp();

			Thread.sleep(1000);

			t.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				Thread.sleep(60000);
				t.tearDown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void test() throws Exception {
		Worker[] workers = new Worker[10];
		for (int i = 0; i < 100; i++) {
			workers[i] = new Worker();
			workers[i].start();
		}
		
		
	}

	class Worker extends Thread {
		public void run() {
			
			for (int i = 0; i < 10000; i++) {
				SyncClient client = null;
				try {
					
					client = (SyncClient) factory.getConnector();
					System.out.println(client + " SNT : 0123456789");
					byte[] response = client.write("0123456789".getBytes());
					System.out.println("RCVD : " + new String(response));
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					factory.release(client);
				}
			}
		}
	}

	public void setUp() throws Exception {

		factory.setUri("client:sync://127.0.0.1:30001?readSize=10&disconnect=false");
		factory.setMaxActive(100);
		factory.start();

	}

	public void tearDown() throws Exception {

		factory.stop();

	}

}
