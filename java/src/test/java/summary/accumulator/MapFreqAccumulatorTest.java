package summary.accumulator;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapFreqAccumulatorTest {
    @Test
    public void testCompress() {
        MapFreqAccumulator acc = new MapFreqAccumulator();
        acc.addRaw(LongLists.mutable.of(1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4));
        acc.compress(3);
        assertEquals(2, 2, acc.values.size());
        assertEquals(5.0, acc.estimate(Lists.mutable.of(1L)).get(0), 1e-10);
    }

}