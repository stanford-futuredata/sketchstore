package summary.compressor.quantile;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import summary.CounterDoubleSketch;

import static org.junit.Assert.*;

public class SkipQuantileCompressorTest {
    @Test
    public void testBiased() {
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        SkipQuantileCompressor comp = new SkipQuantileCompressor(false, 0);
        CounterDoubleSketch sketch = comp.compress(xs, 10);
        assertEquals(5.0, sketch.values[0], 1e-10);
    }

    @Test
    public void testRandomized() {
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        SkipQuantileCompressor comp = new SkipQuantileCompressor(true, 0);
        CounterDoubleSketch sketch = comp.compress(xs, 10);
        assertEquals(10, sketch.values.length);
    }

    @Test
    public void testDuplicate() {
        DoubleArrayList xs = new DoubleArrayList();
        int n = 10;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 1<<i; j++) {
                xs.add(i);
            }
        }
        SkipQuantileCompressor comp = new SkipQuantileCompressor(false, 0);
        CounterDoubleSketch sketch = comp.compress(xs, 10);
        assertTrue(sketch.values.length < n);
    }
}