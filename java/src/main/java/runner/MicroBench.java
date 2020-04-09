package runner;

import org.eclipse.collections.api.map.primitive.MutableLongDoubleMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

import java.util.HashMap;

public class MicroBench
{
  public static void main(String[] args) throws Exception {
    mapBench();
  }

  public static void mapBench() {
    HashMap<Long, Double> counts = new HashMap<>();
    long startTime = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      counts.put((long)i, (double)i);
    }
    long endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    //.15 s for put

    MutableLongDoubleMap eCounts = new LongDoubleHashMap();
    startTime = System.nanoTime();
    for (int i = 0; i < 1_000_000; i++) {
      eCounts.put(i, (float)i);
    }
    endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    // .05 s for put
  }
}
