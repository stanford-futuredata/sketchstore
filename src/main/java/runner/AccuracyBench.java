package runner;

import org.apache.commons.math3.random.JDKRandomGenerator;
import sketches.ApproxBalancedCounterCompressor;
import sketches.BalancedCounterCompressor;
import sketches.CounterCompressor;
import sketches.ExactCountersSketch;
import sketches.counters.KeyCount;
import sketches.RoundingCounterCompressor;
import sketches.TruncateCounterCompressor;
import util.CSVIntDataSource;
import util.GroupedSeqDataSource;
import util.ZipfDataSource;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

public class AccuracyBench
{
  public static void main(String[] args) throws Exception {
    System.out.println("Starting Accuracy Bench");
//    System.in.read();
//    singleSourceBench();
    groupedSourceBench();
  }

  public static void groupedSourceBench() throws Exception {
    System.out.println("Loading Data");
    CSVIntDataSource df = new CSVIntDataSource(
        "/Users/edwardgan/Documents/Projects/datasets/instacart/order_products__prior.csv",
        1
    );
    int n_rows = 5000000;
    df.setLimit(n_rows);
    ArrayList<Integer> rawValues = df.get();
    System.out.println("Exact Count");
    ExactCountersSketch<Integer> ec = new ExactCountersSketch<>();
    for (int x : rawValues) {
      ec.add(x);
    }
    List<Integer> queryRanks = Arrays.asList(
        1, 10, 20, 50, 100
    );
    PriorityQueue<KeyCount<Integer>> orderedItems = new PriorityQueue<>(ec.getSize(), (a, b) -> -a.compareTo(b));
    for (KeyCount<Integer> i : ec.getItems()) {
      orderedItems.offer(i);
    }
    int largestQueryRank = queryRanks.get(queryRanks.size()-1);
    ArrayList<Integer> queryIds = new ArrayList<>(queryRanks.size());
    for (int i = 0; i < largestQueryRank; i++) {
      KeyCount<Integer> curKeyCount = orderedItems.poll();
      if (curKeyCount!=null) {
        queryIds.add(curKeyCount.key);
      }
    }

    System.out.println("Grouping");
    int n_groups = 1000;
    GroupedSeqDataSource<Integer> grouper = new GroupedSeqDataSource<Integer>(n_groups, rawValues);
    ArrayList<ArrayList<Integer>> groups = grouper.getGroups();
    grouper = null;
    rawValues = null;

    List<CounterCompressor<Integer>> compressors = Arrays.asList(
        new BalancedCounterCompressor<>(new JDKRandomGenerator(0)),
        new RoundingCounterCompressor<>(new JDKRandomGenerator(0)),
        new TruncateCounterCompressor<>()
    );

    System.out.println(groups.get(0).get(0));
    System.out.println(groups.size());
  }

  public static void singleSourceBench() throws Exception {
    ZipfDataSource zf = new ZipfDataSource(1.3, 0, 500000);
    ArrayList<Integer> xs = zf.get();
    ExactCountersSketch<Integer> ec = new ExactCountersSketch<>();
    for (int x : xs) {
      ec.add(x);
    }

    List<CounterCompressor<Integer>> compressors = Arrays.asList(
        new BalancedCounterCompressor<>(new JDKRandomGenerator(0)),
        new RoundingCounterCompressor<>(new JDKRandomGenerator(0)),
        new TruncateCounterCompressor<>()
    );
    int sketchSize = 30;
    int numSketches = 1000;
    List<Integer> queryPoints = Arrays.asList(
        1, 10, 20, 50, 100
    );

    for (CounterCompressor<Integer> cc : compressors) {
      System.out.println("Compressor: "+cc.getClass().toString());
      double[] sums = new double[queryPoints.size()];
      double[] sq_sums = new double[queryPoints.size()];
      double[] bias = new double[queryPoints.size()];
      double[] std = new double[queryPoints.size()];

      long startTime = System.nanoTime();
      for (int sketchIdx = 0; sketchIdx < numSketches; sketchIdx++) {
        ExactCountersSketch<Integer> cur_count = new ExactCountersSketch<>();
        Collection<KeyCount<Integer>> curCompressedItems = cc.compress(ec.getItems(), sketchSize);
        cur_count.add(curCompressedItems);
        for (int i = 0; i < queryPoints.size(); i++) {
          int curQuery = queryPoints.get(i);
          int curCount = cur_count.getCount(curQuery);
          sums[i] += curCount;
          sq_sums[i] += curCount*curCount;
        }
      }
      long endTime = System.nanoTime();
      long elapsed = endTime - startTime;
      System.out.println("Elapsed: "+elapsed*1.0e-9);

      for (int i = 0; i < queryPoints.size(); i++) {
        int trueCount = ec.getCount(queryPoints.get(i));
        bias[i] = sums[i]/numSketches - trueCount;
        std[i] = Math.sqrt(sq_sums[i]/numSketches - trueCount*trueCount);
      }
      System.out.println("Biases: "+Arrays.toString(bias));
      System.out.println("Std: "+Arrays.toString(std));
    }
  }
}
