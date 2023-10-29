package com.tsis.connector.client.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.loadbalancer.FailOver;
import com.tsis.connector.client.sync.SyncClient;

public class LoadBalancingFactoryReleaseTest {
	static {
		BasicConfigurator.configure();
	}
	
	private LoadBalancingFactory factory = new LoadBalancingFactory();
	private Server server = null;
	
	public static void main(String[] args) {
		LoadBalancingFactoryReleaseTest t = new LoadBalancingFactoryReleaseTest();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void test() throws Exception {
		
		SyncClient client1 = null;
		SyncClient client2 = null;
		try {
			client1 = (SyncClient)factory.getConnector();
			client2 = (SyncClient)factory.getConnector();
			
			
		} finally {
			factory.release(client1);
			factory.release(client2);
		}
	}
	
	public void setUp() throws Exception {
		
		server = new Server();
		server.start();
		
		factory.setUri("client:sync://127.0.0.1:30002?readSize=10&disconnect=false", "client:sync://127.0.0.1:30001?readSize=10&disconnect=false", "client:sync://127.0.0.1:30001?readSize=10&disconnect=false");
		factory.setLoadbalancer(new FailOver());
		factory.setMaxActive(1);
		factory.start();
			
	}
	public void tearDown() throws Exception {
		
		factory.stop();
		
		if(server != null) {
			server.terminate();
		}
	}
	
	class Server extends Thread {
		private ServerSocket serverSocket = null;
		private InputStream in = null;
		private OutputStream out = null;
		private boolean isStart = true;
		
		public void terminate() {
			isStart = false;
			this.interrupt();
		}
		public void run() {
			try {
				
				serverSocket = new ServerSocket(30001);
				Socket socket = serverSocket.accept();

				while (isStart) {
					in = socket.getInputStream();
					out = socket.getOutputStream();

					while (in.available() <= 0) {
						Thread.sleep(1000);
					}

					byte[] buffer = new byte[(int) in.available()];
					in.read(buffer);
					System.out.println("Server --- read : " + new String(buffer));
					
					//socket.close();
					
					out.write(buffer);
					out.flush();
					System.out.println("Server --- write : " + new String(buffer));
				}
			} catch (InterruptedException e){
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (serverSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
