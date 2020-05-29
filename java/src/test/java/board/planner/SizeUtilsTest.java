package board.planner;

import board.planner.size.SizeUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class SizeUtilsTest {
    @Test
    public void testRound() {
        double[] xs = {.5, .5, .5, .5, .5};
        int[] rxs = SizeUtils.safeRound(xs);
        double roundedTotal = 0;
        double origTotal = 0;
        for (int i = 0; i < xs.length; i++) {
            origTotal += xs[i];
            roundedTotal += rxs[i];
            assertTrue(Math.abs(xs[i] - rxs[i]) <= 1);
        }
        assertEquals(origTotal, roundedTotal, 1);
    }
}