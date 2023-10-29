package com.tsis.connector.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.jboss.netty.handler.codec.http.HttpHeaders;

import com.tsis.connector.client.DriverManager;
import com.tsis.connector.client.sync.SyncClient;

public class HttpClientTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	
	public static void main(String[] args) {
		HttpClientTest t = new HttpClientTest();
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
		Map headers = new HashMap();
		//headers.put("Content-Type","text/plain");
		//headers.put(HttpHeaders.Names.CONTENT_TYPE,"text/html");
		//headers.put(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
		
		//headers.put(HttpHeaders.Names.HOST, "www.tsis.co.kr" );
		//headers.put(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

		headers.put("User-Agent","Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; chromeframe/24.0.1312.52; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; InfoPath.3)");
		headers.put("Accept-Encoding","gzip, deflate");
		headers.put("Accept","application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*");
		headers.put("Accept-Language","ko-KR");
		headers.put("Connection","Keep-Alive");
		
		
		try {
			client = (HttpClient)DriverManager.getClient("client:http://172.16.21.65:50002/token_apverify.jsp?httpType=GET");
			//client = (HttpClient)DriverManager.getClient("client:http://localhost:30000");
			byte[] response = client.write(headers, "0123456789\r\n\0".getBytes());
			
			System.out.println("RCVD : " + new String(new String(response, "euc-kr").getBytes()));
			System.out.println("-------------------------------------");
			System.out.println("*************** CHECK 3 : " + headers.toString());
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

