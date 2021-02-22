package test;

import java.util.*; 
  
public class MyTimer2 { 
  
    public static void main(String args[]) 
    { 
  
        // Creating the timer task, timer 
        Timer time = new Timer(); 
  
        TimerTask timetask = new TimerTask() { 
            public void run() 
            { 
  
                for (int i = 1; i <= 5; i++) { 
  
                    System.out.println("Working on the task"); 
  
                    if (i >= 2) { 
  
                        System.out.println("Stopping the task"); 
                        time.purge(); 
                    } 
                } 
  
                // Purging the timer 
                // System.out.println("The Purge value:"
                //                    + time.purge()); 
            }; 
        }; 
  
        time.schedule(timetask, 1, 1000); 
    } 
} 
