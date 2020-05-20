package summary.compressor.quantile;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import summary.CDFSketch;
import summary.accumulator.SortedQuantileAccumulator;

import static org.junit.Assert.*;

public class CoopQuantileCompressorTest {
    @Test
    public void testSimple() {
        CoopQuantileCompressor compress = new CoopQuantileCompressor();
        int n = 1000;
        DoubleArrayList xs = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        CDFSketch sketch1 = compress.compress(xs, 1);
        CDFSketch sketch2 = compress.compress(xs, 1);
        assertEquals(250.0, Math.abs(sketch2.values.get(0)-500), 1);
    }

    @Test
    public void testMultiSegment() {
        CoopQuantileCompressor compress = new CoopQuantileCompressor();
        int n = 1000;
        DoubleArrayList xs = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        CDFSketch sketch1 = compress.compress(xs, 2);
        CDFSketch sketch2 = compress.compress(xs, 2);

        SortedQuantileAccumulator acc = new SortedQuantileAccumulator();
        acc.addSketch(sketch1);
        acc.addSketch(sketch2);
        assertEquals(1000.0, acc.getCDF().estimate(500.0), 1e-10);
    }
}