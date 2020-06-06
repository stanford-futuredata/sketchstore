package runner;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.collections.api.map.primitive.MutableLongDoubleMap;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class MicroBench
{
  public static void main(String[] args) throws Exception {
//      CoopRuntimeBench.prepare();
//    CoopRuntimeBench.runSB();
//    CoopRuntimeBench.run();
      generateZipf();
  }

  public static void generateZipf() throws IOException {
    RandomGenerator rng = new MersenneTwister(0);
    ZipfDistribution dist = new ZipfDistribution(rng, 100_000_000, 1.1);
    int chunkSize = 100_000_000;
    int numChunks = 10;

    String dirPath = "datasets/zipfbig";
    Timer chunkTime = new Timer();
    for (int chunkNum = 0; chunkNum < numChunks; chunkNum++) {
      chunkTime.reset();
      chunkTime.start();
      Path filePath = Paths.get(dirPath, "zipf-" + chunkNum + ".csv");
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath.toString())));
      for (int i = 0; i < chunkSize; i++) {
        out.println(dist.sample());
      }
      out.close();
      chunkTime.end();
      System.out.println("chunk took: "+chunkTime.getTotalMs());
    }
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
