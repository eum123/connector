package com.tsis.connector.server;

public class ThreadTest {
	
	private Handler handler = new Handler();
	
	public static void main(String[] args) {
		ThreadTest t = new ThreadTest();
		t.test();
	}
	
	public void test() {
		Worker[] worker = new Worker[2];
		for(int i=0 ;i<worker.length ;i++) {
			worker[i] = new Worker("name_" + i);
		}
		for(int i=0 ;i<worker.length ;i++) {
		
			worker[i].start();
		}
	}
	
	class Worker extends Thread {
		private String name = null;
		public Worker(String name) {
			this.name = name;
		}
		public void run() {
			System.out.println(name + " start " + handler.hashCode());
			handler.sleep();
			System.out.println(name + " end");
		}
	}
	
	class Handler {
		public void sleep() {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
