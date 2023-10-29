package performance;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.sync.SyncClient;

public class Worker extends Thread {

	private int num = 0;
	private SyncClient client = null;
	private int maxMsgCount = 0;
	private String testSndMsg_1 = new String("0021^20150330002448^^0001");
	private String testSndMsg_2 = new String("00620050^20150331002450^ON^0001^0004^1730521478^0^ghd6704^wk794613");
	TestClass parent;
	int sleepNum = -1;
	boolean flag = false;

	public Worker(int maxMsgCount , TestClass parent) {
		this.maxMsgCount = maxMsgCount;
		this.parent = parent;
		try {
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		//maxMsgCount = 10;
		
		for(int num = 0 ; num < maxMsgCount ; num++) {
			try {
				long startTime = System.currentTimeMillis();
				client = (SyncClient) DriverManager
						.getClient("client:sync://59.13.0.15:20046?type=variable&readStart=0&readSize=4&readHeaderLength=4&disconnect=true");

				if (true) {
					client.write(testSndMsg_2.getBytes());
				} else {
					client.write(testSndMsg_1.getBytes());
				}
				long endTime = System.currentTimeMillis();
				
				long dif = endTime - startTime;

				System.out.println(">> "+dif);
				
				Thread.sleep(Math.abs(1000 - dif));
				
				///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				if(dif > 10000) {
					parent.isLong = true;
				}
				
				// client.stop();
			} catch (Exception e) {
				
				if(e.getMessage().indexOf("connection closed") >= 0) {
					
				} else {
					parent.isLong = true;
					e.printStackTrace();
				}
				
			} finally {
				try {
					client.stop();
					
					if(parent.isLong) {
						System.out.println("-----------------------------------------------------");
						Thread.sleep(10000);
						parent.isLong = false;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}
//		while (num < maxMsgCount) {
//			try {
//
//				if ((num % 20) == 0) {
//					client.write(testSndMsg_2.getBytes());
//				} else {
//					client.write(testSndMsg_1.getBytes());
//				}
//				// client.stop();
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				num++;
//			}
//		}
		
		System.out.println("END");

	}

}
