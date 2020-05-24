package summary.accumulator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

public class SortedQuantileAccumulatorTest {
    @Test
    public void testSimple() {
        SortedQuantileAccumulator acc = new SortedQuantileAccumulator();
        acc.addRaw(DoubleLists.mutable.of(1.0, 3.0, 5.0, 7.0, 9.0));
        acc.addRaw(DoubleLists.mutable.of(2.0, 3.0, 3.0, 5.0, 7.0, 8.0));
        assertEquals(11.0, acc.weights.sum(), 1e-10);
        assertEquals(7, acc.items.size());
    }

    @Test
    public void testCompress() {
        SortedQuantileAccumulator acc = new SortedQuantileAccumulator();
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        acc.addRaw(xs);
        acc.compress(10);
        assertEquals(10, acc.items.size());
    }

    @Test
    public void testDelta() {
        DoubleArrayList xs = DoubleArrayList.newListWith(1.0, 2.0, 3.0);
        DoubleArrayList pdf = DoubleArrayList.newListWith(1.0, 2.0, 2.0);

        DoubleArrayList xs2 = DoubleArrayList.newListWith(1.0, 2.0, 2.5, 3.0);
        DoubleArrayList pdf2 = DoubleArrayList.newListWith(1.0, 1.0, 1.0, 1.0);

        SortedQuantileAccumulator trueCDF = new SortedQuantileAccumulator(xs2, pdf2);
        SortedQuantileAccumulator estCDF = new SortedQuantileAccumulator(xs, pdf);
        double[] delta = trueCDF.calcDelta(estCDF);
        assertEquals(delta.length, trueCDF.items.size());
    }

    @Test
    public void testEstimate() {
        SortedQuantileAccumulator acc = new SortedQuantileAccumulator();
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