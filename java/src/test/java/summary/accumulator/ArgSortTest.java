package summary.accumulator;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import runner.Timer;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class ArgSortTest {
    @Test
    public void testSimple() {
        double[] xs = {3.0, 1.0, 2.0, 4.0};
        DoubleArrayList xList = new DoubleArrayList(xs);
        int[] args = ArgSort.argSort(xList, 0, xs.length);
        assertEquals(1, args[0]);
    }

    @Test
    public void testTiming() {
        Random r = new Random(0);
        int n = 100_000;
        double[] xs = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = r.nextDouble();
        }
        DoubleArrayList xList = new DoubleArrayList(xs);

        Timer t =  new Timer();
        t.start();
        int[] args1= ArgSort.argSort(xList);
        t.end();
        double time1 = t.getTotalMs();

        t.reset();
        t.start();
        int[] args2= ArgSort.argSortSlow2(xList);
        t.end();
        double time2 = t.getTotalMs();

        assertArrayEquals(args1, args2);
        assertTrue(time1 < time2);

    }

}