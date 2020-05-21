package summary.accumulator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapQuantileAccumulatorTest {
    @Test
    public void testSimple() {
        MapQuantileAccumulator acc = new MapQuantileAccumulator();
        DoubleList xs = DoubleLists.mutable.of(1.0, 1.0, 2.0, 3.0, 3.0);
        acc.addRaw(xs);
        DoubleList ranks = acc.estimate(Lists.mutable.of(2.0, 3.0));
        assertEquals(3.0, ranks.get(0), 1e-10);
    }

    @Test
    public void testEstimate() {
        MapQuantileAccumulator acc = new MapQuantileAccumulator();
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        acc.addRaw(xs);
        DoubleList rankEstimates = acc.estimate(Lists.mutable.of(33.0, 77.0));
        assertEquals(2, rankEstimates.size());
        assertEquals(34.0, rankEstimates.get(0), 1e-10);
    }

}