package even;

public class Main {

	public static void main(String[] args) {
		EvenGenerator generator = new EvenGenerator();
		Thread t1 = new Thread(new EvenChecker(generator));
		Thread t2 = new Thread(new EvenChecker(generator));
		Thread t3 = new Thread(new EvenChecker(generator));
		Thread t4 = new Thread(new EvenChecker(generator));
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
	}
}
