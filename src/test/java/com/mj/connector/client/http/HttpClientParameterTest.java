package com.mj.connector.client.http;

import com.tsis.connector.client.http.HttpClient;
import org.apache.log4j.BasicConfigurator;

import com.tsis.connector.client.DriverManager;

public class HttpClientParameterTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	public static void main(String[] args) {
		HttpClientParameterTest t = new HttpClientParameterTest();
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
			client = (HttpClient)DriverManager.getClient("client:http://gw.tsis.co.kr:8080/iris_tks/LoginHandler");
			//client = (HttpClient)DriverManager.getClient("client:http://localhost:10004/iris_tks/LoginHandler");
			byte[] response = client.write("login_locale=ko&id=8001667&passwd=ahdusqja1".getBytes());
			
			System.out.println("RCVD : " + new String(new String(response, "euc-kr").getBytes()));
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
