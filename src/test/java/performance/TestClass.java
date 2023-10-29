package performance;

public class TestClass {
	int userCount = 1;
	int maxMsgCount = 1;
	
	protected boolean isLong = false;
	
	public TestClass() {
		Worker workers[] = new Worker[userCount];
		for(int i = 0 ; i < userCount ; i++) {
			workers[i] = new Worker(maxMsgCount, this);
			workers[i].start();
		}
		
	}

	public static void main(String[] args) {
		TestClass clazz = new TestClass();

	}
	
	

}
