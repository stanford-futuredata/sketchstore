package summary.compressor.freq;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class HaircombCompressorTest {
    @Test
    public void testSimple() {
        LongDoubleHashMap xv = new LongDoubleHashMap();
        xv.put(1, 100.0);
        xv.put(2, 50.0);
        xv.put(3, 33.0);
        xv.put(4, 33.0);
        xv.put(5, 20.0);
        HaircombCompressor tc = new HaircombCompressor(0);
        LongDoubleHashMap xc = tc.compress(xv, 3);
        assertEquals(3, xc.size());
        assertEquals(100.0, xc.get(1l), 1e-10);
    }

}