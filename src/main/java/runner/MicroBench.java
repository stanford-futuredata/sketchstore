package runner;

import java.util.HashMap;

public class MicroBench
{
  public static void main(String[] args) throws Exception {
    HashMap<Object, Integer> counts = new HashMap<>();
    for (int i = 0; i < 100000; i++) {
      counts.put(i, i);
    }
    long startTime = System.nanoTime();
    for (int i = 0; i < 100000; i++) {
//      counts.put(i, counts.get(i)+i);
      counts.merge(i, i, (a,b) -> a+b);
    }
    long endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    //.01 s for put get
    //.04 s for merge
  }
}
