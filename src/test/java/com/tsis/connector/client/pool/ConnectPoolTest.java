package com.tsis.connector.client.pool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.loadbalancer.FailOver;
import com.tsis.connector.client.sync.SyncClient;

public class ConnectPoolTest {
	static {
		BasicConfigurator.configure();
	}
	
	private ConnectorPoolFactory factory = new ConnectorPoolFactory();
	//private LoadBalancingFactory factory = new LoadBalancingFactory();
	private Server server = null;
	
	public static void main(String[] args) {
		ConnectPoolTest t = new ConnectPoolTest();
		try {
			t.setUp();
			
			Thread.sleep(1000);
			
			t.test();
			Thread.sleep(1000000);
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
		
		SyncClient client = null;
		
		
		//Thread.sleep(50000000);
		try {
			client = (SyncClient)factory.getConnector();
			byte[] response = null;
			for(int i = 0 ; i < 10 ; i++ ) {
				response = client.write("0123456789".getBytes());
			}
			System.out.println("RCVD : " + new String(response));
		} finally {
			factory.release(client);
		}
	}
	
	public void setUp() throws Exception {
		
		//server = new Server();
		//server.start();
		
		factory.setUri("client:sync://127.0.0.1:55000?readSize=10&disconnect=false");
		factory.setMaxActive(10);
		//factory.setMaxIdle(10);
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
