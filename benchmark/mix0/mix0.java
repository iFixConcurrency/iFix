/**
 * This package is for testing MCR under RMMs
 */
/**
 * @author Alan
 *
 */
package mix0;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

public class mix0 {
	//private int z;
	 static int x;
	 static int y;
	 static int z=0;
	//private static Object lock = new Object();
	//private static int a=0;
	 static int b=0;

	public static void main(String[] args) {
		
		int a = 0;	
//		testTSO2 test = new testTSO2();
//		test.z =0;
		
		x = 0;
		//x = 1;
		y = 0;
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				
				
				y = 1;
				b = x;
				
			}
			

		});
		t2.start();
		
		//z = 1;
		//z = 1;
		x = 1;
		a = y;
		//b = y;
		//int c = y;
		
		try {
			t2.join();
			System.out.println("a= " + a+ ","+ "b= "+b);
			if(a==0 && b==0){
				System.out.println("error");
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {
		
		mix0.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}