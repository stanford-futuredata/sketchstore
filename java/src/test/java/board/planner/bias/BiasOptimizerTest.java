package board.planner.bias;

import board.planner.bias.BiasOptimizer;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class BiasOptimizerTest {
    @Test
    public void testManual() {
        long[] occCounts = {1, 2, 3, 5};
        long[] occCountFreqs = {50, 30, 10, 10};
        RMSErrorFunction.SegmentCCDF ccdf = new RMSErrorFunction.SegmentCCDF(
                occCounts,
                occCountFreqs
        );
        FastList<RMSErrorFunction.SegmentCCDF> segments = new FastList<>();
        int nSegments = 10;
        int[] segSpace = new int[nSegments];
        for (int i = 0; i < nSegments; i++) {
            segments.add(ccdf);
            segSpace[i] = 5;
        }
        RMSErrorFunction f = new RMSErrorFunction(
                segments,
                segSpace
        );

        BFGSOptimizer opt = new BFGSOptimizer(f);
        opt.setVerbose(false);
        double[] xSolve = new double[nSegments];
        xSolve = opt.solve(xSolve, 1e-5);
        assertTrue(xSolve[0] > 0);
    }
}