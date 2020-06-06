package summary.compressor.freq;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import org.junit.Test;
import summary.CounterLongSketch;
import summary.compressor.freq.CoopFreqCompressor;

import static org.junit.Assert.*;

public class CoopFreqCompressorTest {
    @Test
    public void testSimple() {
        int size = 2;
        CoopFreqCompressor cf = new CoopFreqCompressor(0);

        LongDoubleHashMap counts = new LongDoubleHashMap();
        counts.put(1, 10.0);
        counts.put(2, 5.0);
        counts.put(3, 3.0);
        counts.put(4, 2.0);

        CounterLongSketch out;
        out = cf.compress(counts, size);
        assertEquals(10.0, out.estimate(1L), 1e-10);
        out = cf.compress(counts, size);
        assertEquals(6.0, out.estimate(3L), 1e-10);
    }
}