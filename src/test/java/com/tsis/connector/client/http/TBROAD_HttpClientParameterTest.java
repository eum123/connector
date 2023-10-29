package com.tsis.connector.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;

import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.sync.SyncClient;

public class TBROAD_HttpClientParameterTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	public static void main(String[] args) {
		TBROAD_HttpClientParameterTest t = new TBROAD_HttpClientParameterTest();
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
		HttpClient client = null;
		try {
			client = (HttpClient)DriverManager.getClient("client:http://erp.tbroad.com/select.do?httpType=GET&uriEncoding=euc-kr");
			//client = (HttpClient)DriverManager.getClient("client:http://localhost:10004/iris_tks/LoginHandler");
			byte[] response = client.write("queryType=SQL&fileXml=tw/twda&queryId=sel_twda01e5_cooper_mast&parmValue=com_id=A410∥st_dt=2014-12-01∥ed_dt=2014-12-12∥repo_cd=∥stat=∥login_user=0∥jdbc=response".getBytes());
			//byte[] response = client.write(("queryType=SQL&fileXml=tw/twda&queryId=" + URLEncoder.encode("sel_twda01e5_cooper_mast&parmValue=com_id=A410∥st_dt=2014-12-01∥ed_dt=2014-12-12∥repo_cd=∥stat=∥login_user=0∥jdbc=response")).getBytes("EUC-KR"));
			
			System.out.println("RCVD : " + new String(response, "euc-kr"));
		} finally {
			if(client != null) {
				client.stop();
			}
		}
	}
	
	public void setUp() {
		
		
	}
	public void tearDown() {
	}
	
	
}
