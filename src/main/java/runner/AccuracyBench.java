package runner;

import org.apache.commons.math3.random.JDKRandomGenerator;
import sketches.BalancedCounterCompressor;
import sketches.CounterCompressor;
import sketches.ExactCountersSketch;
import sketches.counters.KeyCount;
import sketches.RoundingCounterCompressor;
import sketches.TruncateCounterCompressor;
import util.ZipfDataSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AccuracyBench
{
  public static void main(String[] args) throws Exception {
    System.out.println("Starting Accuracy Bench");
    System.in.read();
    singleSourceBench();
  }

  public static void singleSourceBench() throws Exception {
    ZipfDataSource zf = new ZipfDataSource(1.3, 0, 100000);
    int[] xs = zf.getInt();
    ExactCountersSketch<Integer> ec = new ExactCountersSketch<>();
    for (int x : xs) {
      ec.add(x);
    }

    List<CounterCompressor<Integer>> compressors = Arrays.asList(
        new BalancedCounterCompressor<>(new JDKRandomGenerator(0)),
        new RoundingCounterCompressor<>(new JDKRandomGenerator(0)),
        new TruncateCounterCompressor<>()
    );
    int sketchSize = 50;
    int numSketches = 10000;
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
