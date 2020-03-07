// Copyright (c) 2003 Webware Consulting
package elevator;
import java.util.*;
public class Simulator  {
   public static boolean debug = false;
   public static int MAX_PEOPLE = 2; //200;
   public static final int MAX_TIME = 60;
   private static volatile int counter;
   public static void stopProgram(){
      counter = MAX_TIME;
   }
   public static int getTimeRemaing(){
       return (MAX_TIME - counter)/2;
   }
   public static int getElapsedTime(){
       return counter/2;
   }
   public static void main(String[] args)  {
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY+2);
      //String source = null;
      //if(args.length >0)
      //   source = args[0];
      //ElevatorConsole console = new ElevatorConsole(source);
      
      // ZL - one test param determining the number of people
      if (args.length != 1){
    	  System.out.println("One integer parameter determining number of persons (threads) is needed.");
    	  return;
      }
      MAX_PEOPLE = Integer.parseInt(args[0]);
    	  
      Vector people = new Vector();
      Building b = new Building();
      //console.setBuilding(b);
      Person.setBuilding(b);
      for(int i = 0; i < MAX_PEOPLE; i++){
         Person p = new Person(i+1);
         people.add(p);
         p.start();
      }
      //for(counter = 0; counter <= MAX_TIME; counter++){
         //console.updateDisplay();
         //try{
         //   Thread.currentThread().sleep(500);
         //}catch(Exception e){
         //   e.printStackTrace();
         // }
      //}
      
      //ZL give some time to persons - 5 sec
      try{
    	  Thread.sleep(5000);
      } catch (InterruptedException ie){
    	  // DO NOTHING
      }
      
      for(int i = 0; i < people.size(); i++){
         ((Person)people.get(i)).setStopRunning();
      }
      b.stopElevators();
      //System.out.println("Progam ended");
    }
    
} // end Simulator



