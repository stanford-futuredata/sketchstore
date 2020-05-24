package summary.compressor.quantile;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import summary.CounterDoubleSketch;

import static org.junit.Assert.*;

public class USampleQuantCompressorTest {
    @Test
    public void testSimple() {
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        SeqCounterCompressor comp = new USampleQuantCompressor(0);
        CounterDoubleSketch sketch = comp.compress(xs, 10);
        assertEquals(10, sketch.values.length);
        assertEquals(100.0, sketch.estimate(101.0), 1e-10);
    }
}