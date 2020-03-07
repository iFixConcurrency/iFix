/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         * 
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                                                                         *
*      JGFInstrumentor.class version copyright (c) The University of Edinburgh, 1999.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/


package JGFMonteCarloBenchSizeA.jgfutil;

import java.util.*;

import JGFMonteCarloBenchSizeA.jgfutil.JGFTimer;

public class JGFInstrumentor{

  private static Hashtable timers;
  private static Hashtable data; 

  static {
    timers = new Hashtable();
    data = new Hashtable(); 
  }

  public static void addTimer (String name){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			System.out.println("JGFInstrumentor.addTimer: warning -  timer " + name + " already exists");
		} else {
			timers.put(name, new JGFTimer(name));
		}
	}
}
    
  public static void addTimer (String name, String opname){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			System.out.println("JGFInstrumentor.addTimer: warning -  timer " + name + " already exists");
		} else {
			timers.put(name, new JGFTimer(name, opname));
		}
	}
}

  public static void addTimer (String name, String opname, int size){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			System.out.println("JGFInstrumentor.addTimer: warning -  timer " + name + " already exists");
		} else {
			timers.put(name, new JGFTimer(name, opname, size));
		}
	}
}

  public static void startTimer(String name){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).start();
		} else {
			System.out.println("JGFInstrumentor.startTimer: failed -  timer " + name + " does not exist");
		}
	}
}

  public static void stopTimer(String name){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).stop();
		} else {
			System.out.println("JGFInstrumentor.stopTimer: failed -  timer " + name + " does not exist");
		}
	}
}

  public static void addOpsToTimer(String name, double count){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).addops(count);
		} else {
			System.out.println("JGFInstrumentor.addOpsToTimer: failed -  timer " + name + " does not exist");
		}
	}
}  

  public static void addTimeToTimer(String name, double added_time){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).addtime(added_time);
		} else {
			System.out.println("JGFInstrumentor.addTimeToTimer: failed -  timer " + name + " does not exist");
		}
	}
}

  public static double readTimer(String name){
	synchronized (JGFInstrumentor.class) {
		double time;
		if (timers.containsKey(name)) {
			time = ((JGFTimer) timers.get(name)).time;
		} else {
			System.out.println("JGFInstrumentor.readTimer: failed -  timer " + name + " does not exist");
			time = 0.0;
		}
		return time;
	}
}  

  public static void resetTimer(String name){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).reset();
		} else {
			System.out.println("JGFInstrumentor.resetTimer: failed -  timer " + name + " does not exist");
		}
	}
}
  
  public static void printTimer(String name){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).print();
		} else {
			System.out.println("JGFInstrumentor.printTimer: failed -  timer " + name + " does not exist");
		}
	}
}
  
  public static void printperfTimer(String name){
	synchronized (JGFInstrumentor.class) {
		if (timers.containsKey(name)) {
			((JGFTimer) timers.get(name)).printperf();
		} else {
			System.out.println("JGFInstrumentor.printTimer: failed -  timer " + name + " does not exist");
		}
	}
}
  
  public static void storeData(String name, Object obj){
	synchronized (JGFInstrumentor.class) {
		data.put(name, obj);
	}
}

  public static void retrieveData(String name, Object obj){
	synchronized (JGFInstrumentor.class) {
		obj = data.get(name);
	}
}

  public static void printHeader(int section, int size,int nthreads) {
	synchronized (JGFInstrumentor.class) {
		String header, base;
		header = "";
		base = "Java Grande Forum Thread Benchmark Suite - Version 1.0 - Section ";
		switch (section) {
		case 1:
			header = base + "1";
			break;
		case 2:
			switch (size) {
			case 0:
				header = base + "2 - Size A";
				break;
			case 1:
				header = base + "2 - Size B";
				break;
			case 2:
				header = base + "2 - Size C";
				break;
			}
			break;
		case 3:
			switch (size) {
			case 0:
				header = base + "3 - Size A";
				break;
			case 1:
				header = base + "3 - Size B";
				break;
			}
			break;
		}
		System.out.println(header);
		if (nthreads == 1) {
			System.out.println("Executing on " + nthreads + " thread");
		} else {
			System.out.println("Executing on " + nthreads + " threads");
		}
		System.out.println("");
	}
} 

}
