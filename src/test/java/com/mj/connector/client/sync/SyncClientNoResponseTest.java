package com.mj.connector.client.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.sync.SyncClient;

public class SyncClientNoResponseTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	private Server server = null;
	public static void main(String[] args) {
		SyncClientNoResponseTest t = new SyncClientNoResponseTest();
		try {
			t.setUp();
			
			Thread.sleep(1000);
			
			t.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			t.tearDown();
		}
	}
	
	public void test() throws Exception {
		SyncClient client = null;
		try {
			client = (SyncClient)DriverManager.getClient("client:sync://127.0.0.1:30001?readSize=10&disconnect=true");
			byte[] response = client.write("0123456789".getBytes());
			
			System.out.println("RCVD : " + new String(response));
		} finally {
			if(client != null) {
				client.stop();
			}
		}
	}
	
	public void setUp() {
		server = new Server();
		server.start();
		
		
	}
	public void tearDown() {
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
					
					socket.close();
					
					
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
