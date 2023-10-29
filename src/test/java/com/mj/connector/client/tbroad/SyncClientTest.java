package com.mj.connector.client.tbroad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.sync.SyncClient;

public class SyncClientTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	private Server server = null;
	public static void main(String[] args) {
		SyncClientTest t = new SyncClientTest();
		try {
			t.setUp();
			
			Thread.sleep(1000);
			
			t.test();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		//	t.tearDown();
		}
	}
	
	public void test() throws Exception {
		SyncClient client = null;
		try {
			client = (SyncClient)DriverManager.getClient("client:sync://172.16.180.224:30004?type=variable&readStart=12&readSize=5&disconnect=false");
			
			System.out.println("-----------------------------------");
			byte[] response = client.write("12O2iOEw1TIB01979 TIB00FEP0000      S00000020150526153519jCRvbcXp        000000000000                                                                           <?xml version=\"1.0\" encoding=\"UTF-8\"?><BMT><BMTInfo><SVCNAME>INTERPARK</SVCNAME><DOC_SEQ>00000146</DOC_SEQ><TRCODE>TBNC</TRCODE><DOC_CODE>10000</DOC_CODE><DOC_DT>20150325085456</DOC_DT><REQ_TYPE>20150316</REQ_TYPE><REQ_CNT>19</REQ_CNT><REQ><ID_REQUEST>20150513013503000001</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000002</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000003</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000004</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000005</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000006</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000007</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000009</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000010</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000011</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000012</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000013</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000014</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000015</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000016</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000017</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000018</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000019</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000021</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000022</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000023</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000024</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000026</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000027</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000028</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000029</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000030</ID_REQUEST></REQ><REQ><ID_REQUEST>20150513013503000031</ID_REQUEST></REQ></BMTInfo></BMT>".getBytes());
			
			
			System.out.println("RCVD : " + new String(response));
		} finally {
			if(client != null) {
				client.stop();
			}
		}
	}
	
	public void setUp() {
		//server = new Server();
		//server.start();
		
		
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
