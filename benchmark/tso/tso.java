/**
 * This package is for testing MCR under RMMs
 */
/**
 * @author Alan
 *
 */
package tso;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

public class tso {
	static int x = 0, y = 0;
	public static void main(String[] args) {		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {			
				y = 1;
				int b = x;
				
			}
			

		});
		t1.start();
		
		x = 1;
		int a = y;
		
		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() throws InterruptedException {
		try {	
			tso.main(null);
		} catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}
}