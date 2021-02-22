package test;
import java.util.LinkedList;
import java.util.Queue;

public class MyQueue {
    public static void main(String[] args) {
        Queue<String> q = new LinkedList<>(); 
        q.add("Eaan");
        q.add("Sharon");
        q.add("Deb");
        System.out.println("Original queue:" + q);
        q.poll();
        System.out.println("First item removed: " + q);
       System.out.println("Second item: " + q.toArray()[1]);
    }
    
}
