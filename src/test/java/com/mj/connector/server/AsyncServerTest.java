package com.mj.connector.server;

import com.tsis.connector.server.ListenerManager;
import com.tsis.connector.server.ServerMessageHandler;
import org.apache.log4j.BasicConfigurator;
import org.jboss.netty.channel.Channel;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.MessageHandler;
import com.tsis.connector.client.async.AsyncClient;
import com.tsis.connector.server.async.AsyncServer;

public class AsyncServerTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	private AsyncServer server = null;
	public static void main(String[] args) {
		AsyncServerTest t = new AsyncServerTest();
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
			client = (AsyncClient)DriverManager.getClient("client:async://127.0.0.1:30001?readSize=10&disconnect=true");
			
			client.setMessageHandler(new MessageHandler() {

				public void messageReceived(byte[] message) {
					System.out.println("RCVD : " + new String(message));
				}

				public void messageSent(byte[] message) {
					
					System.out.println("SENT : " + new String(message));
				}

				public void exceptionCaught(Throwable cause) {
					cause.printStackTrace();
				}
				
			});
			
			client.write("0123456789".getBytes());
			
			
			Thread.sleep(5000);
			
		} finally {
			if(client != null) {
				client.stop();
			}
		}
	}
	
	public void setUp() throws Exception {
		
		server = (AsyncServer) ListenerManager.getServer("server:async://127.0.0.1:30001?readSize=10");
		
		server.setMessageHandler(new ServerMessageHandler() {

			public void messageReceived(Channel channel, byte[] message) {
				System.out.println("Server RCVD : " + new String(message));
				channel.write(message);
			}

			public void messageSent(Channel channel, byte[] message) {
				System.out.println("Server SNT : " + new String(message));
			}

			public void exceptionCaught(Channel channel, Throwable cause) {
				cause.printStackTrace();
			}

			public void channelOpen(Channel channel) {
				// TODO Auto-generated method stub
				
			}

			public void channelClosed(Channel channel) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
	public void tearDown() throws Exception{
		if(server != null) {
			server.stop();
		}
	}
	
	
}
