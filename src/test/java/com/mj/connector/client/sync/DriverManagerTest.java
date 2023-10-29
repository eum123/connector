package com.mj.connector.client.sync;

import com.tsis.connector.client.DriverManager;

public class DriverManagerTest {
	
	public static void main(String[] args) {
		DriverManagerTest t = new DriverManagerTest();
		t.test();
	}
	
	public void test() {
		try {
			DriverManager.getClient("client:sync://127.0.0.1:12345?readSize=10");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
