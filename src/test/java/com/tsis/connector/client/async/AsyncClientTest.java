package com.tsis.connector.client.async;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.MessageHandler;
import com.tsis.connector.client.async.*;



public class AsyncClientTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	private static Logger log = LoggerFactory.getLogger(AsyncClientTest.class);
	
	
	private Server server = null;
	public static void main(String[] args) {
		AsyncClientTest t = new AsyncClientTest();
		try {
			//t.setUp();
			
			Thread.sleep(1000);
			
			t.test();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			t.tearDown();
		}
	}
	
	public void test() throws Exception {
		AsyncClient client = null;
		try {
			client = (AsyncClient)DriverManager.getClient("client:async://127.0.0.1:60000?readSize=10&disconnect=false");
			//client = (AsyncClient)DriverManager.getClient("client:async://172.70.5.137:55000?readSize=10&disconnect=false");
			

			log.debug("WRTIE 1");
			client.write("0123456789".getBytes());
			client.write("0123456789".getBytes());
			client.write("0123456789".getBytes());
			client.write("0123456789".getBytes());
			
			log.debug("WRTIE 2");
			
			//Thread.sleep(5000);
			
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

