package com.tsis.connector.server;

import org.jboss.netty.channel.Channel;

import com.tsis.connector.server.async.AsyncServer;

public class TestServer {
	
	private AsyncServer server = null;
	
	public void test() throws Exception {
		server = (AsyncServer)ListenerManager.getServer("server:async://172.70.5.125:30001?readSize=10");
		
		
		server.setMessageHandler(new ServerMessageHandler() {

			public void messageReceived(Channel channel, byte[] message) {
				System.out.println("Server RCVD : " + new String(message));
				channel.write(message);
				System.out.println("Server SNT : " + new String(message));
			}

			public void messageSent(Channel channel, byte[] message) {
				System.out.println("Server SNT : " + new String(message));
			}

			public void exceptionCaught(Channel channel, Throwable cause) {
				System.out.println("--------------" + cause);
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
	
	public static void main(String[] args) {
		TestServer t = new TestServer();
		try {
			t.test();
			Thread.sleep(6000000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
}
