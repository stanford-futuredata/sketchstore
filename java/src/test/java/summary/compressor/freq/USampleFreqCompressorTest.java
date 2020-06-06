package summary.compressor.freq;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import org.junit.Test;
import summary.CounterLongSketch;

import static org.junit.Assert.*;

public class USampleFreqCompressorTest {
    @Test
    public void testSimple() {
        LongDoubleHashMap xv = new LongDoubleHashMap();
        xv.put(1, 100.0);
        xv.put(2, 50.0);
        xv.put(3, 33.0);
        xv.put(4, 33.0);
        xv.put(5, 20.0);
        USampleFreqCompressor tc = new USampleFreqCompressor(0);
        CounterLongSketch xc = tc.compress(xv, 10);
        assertTrue(xc.size() <= 10);
    }
}