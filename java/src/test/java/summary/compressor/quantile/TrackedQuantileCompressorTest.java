package summary.compressor.quantile;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import summary.CDFSketch;

import static org.junit.Assert.*;

public class TrackedQuantileCompressorTest {
    @Test
    public void testSimple() {
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        DoubleList xTracked = DoubleLists.mutable.of(
                10.0, 75.0
        );
        SeqDictCompressor comp = new TrackedQuantileCompressor(xTracked);
        CDFSketch sketch = comp.compress(xs, 10);
        assertEquals(2, sketch.values.size());
        assertEquals(76.0, sketch.estimate(101.0), 1e-10);
    }

}