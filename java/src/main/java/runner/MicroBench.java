package runner;

import org.eclipse.collections.api.map.primitive.MutableLongDoubleMap;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.compressor.CoopFreqCompressor;

import java.util.ArrayList;
import java.util.HashMap;

public class MicroBench
{
  public static void main(String[] args) throws Exception {
//      CoopRuntimeBench.prepare();
    CoopRuntimeBench.runSB();
//    CoopRuntimeBench.run();
  }

  public static void arrayBench() {
    int n = 10_000_000;

    ArrayList<Integer> items = new ArrayList<Integer>();
    long startTime = System.nanoTime();
    for (int i = 0; i < n; i++) {
      items.add(i);
    }
    long endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    // .57

    IntArrayList eItems = new IntArrayList();
    startTime = System.nanoTime();
    for (int i = 0; i < n; i++) {
      eItems.add(i);
    }
    endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    // .10

    eItems = new IntArrayList(n);
    startTime = System.nanoTime();
    for (int i = 0; i < n; i++) {
      eItems.add(i);
    }
    endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    // .02

    int[] aItems = new int[n];
    startTime = System.nanoTime();
    for (int i = 0; i < n; i++) {
      aItems[i] = i;
    }
    endTime = System.nanoTime();
    System.out.println((endTime - startTime)*1.0e-9);
    // .01
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
