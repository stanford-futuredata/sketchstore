package sketches;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.junit.Test;
import sketches.counters.KeyCount;
import util.ZipfDataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FrequentItemsTest
{
  @Test
  public void testSimple() throws IOException {
    ZipfDataSource zf = new ZipfDataSource(1.3, 0, 10000);

    SpaceSavingSketch<Integer> ss = new SpaceSavingSketch<>(50);
    ExactCountersSketch<Integer> ec = new ExactCountersSketch<>();
    ArrayList<Integer> xs = zf.get();
    for (int x : xs) {
      ss.add(x);
      ec.add(x);
    }

    int expectedError = ss.getError();
    assertTrue(expectedError < xs.size()/ ss.getSize());

    for (KeyCount<Integer> x : ec.getItems()) {
      int trueCount = x.count;
      int estCount = ss.getCount(x.key);
      assertTrue(estCount - trueCount <= expectedError);
    }
  }

  @Test
  public void testCompression() throws IOException {
    ZipfDataSource zf = new ZipfDataSource(1.3, 0, 10000);
    int sketchSize = 50;

    ExactCountersSketch<Integer> ec = new ExactCountersSketch<>();
    ArrayList<Integer> xs = zf.get();
    for (int x : xs) {
      ec.add(x);
    }

    List<CounterCompressor<Integer>> ccs = Arrays.asList(
        new BalancedCounterCompressor<>(new JDKRandomGenerator(0)),
        new RoundingCounterCompressor<>(new JDKRandomGenerator(0)),
        new TruncateCounterCompressor<>()
    );
    for (CounterCompressor<Integer> cc : ccs) {
      ExactCountersSketch<Integer> ec_compressed = new ExactCountersSketch<>();
      ec_compressed.add(cc.compress(ec.getItems(), sketchSize));
      System.out.println(cc.getClass());
      System.out.println(cc.getMaxError());
      assertTrue(ec_compressed.getSize() <= sketchSize);
      assertEquals(ec.getCount(1), ec_compressed.getCount(1));
    }
  }
}