package summary.accumulator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class ListQuantileAccumulatorTest {
    @Test
    public void testEstimate() {
        ListQuantileAccumulator acc2 = new ListQuantileAccumulator();
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        acc2.addRaw(xs);
        DoubleList rankEstimates2 = acc2.estimate(Lists.mutable.of(33.0, 33.5, 77.0));
        assertEquals(3, rankEstimates2.size());
        assertEquals(34.0, rankEstimates2.get(0), 1e-10);
        assertEquals(rankEstimates2.get(1), rankEstimates2.get(0), 1e-10);
    }

    @Test
    public void testRandom() {
        ListQuantileAccumulator acc2 = new ListQuantileAccumulator();
        Random r = new Random(0);
        DoubleArrayList xs = new DoubleArrayList();
        int n = 100;
        for (int i = 0; i < n; i++) {
            xs.add(r.nextDouble());
        }
        acc2.addRaw(xs);

        double trueRank = 0;
        for (int i = 0; i < n; i++) {
            if (xs.get(i) <= .1) {
                trueRank++;
            }
        }

        DoubleList rankEstimates2 = acc2.estimate(Lists.mutable.of(.1, .5, .9));
        assertEquals(trueRank, rankEstimates2.get(0), 1e-10);
    }

}