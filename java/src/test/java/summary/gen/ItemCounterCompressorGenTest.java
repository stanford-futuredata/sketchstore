package summary.gen;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;
import summary.Sketch;
import summary.compressor.freq.HaircombCompressor;

import static org.junit.Assert.*;

public class ItemCounterCompressorGenTest {
    @Test
    public void testSimple() {
        ItemCounterCompressorGen gen = new ItemCounterCompressorGen(
                new HaircombCompressor(0)
        );
        long[] xs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11};
        FastList<Sketch<Long>> sketches = gen.generate(new LongArrayList(xs), 1, 1);
        assertEquals(2.0, sketches.get(0).estimate(11L), 1e-10);
        assertEquals(0.0, sketches.get(0).estimate(1L), 1e-10);
    }
}