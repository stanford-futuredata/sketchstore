package board.planner.bias;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.tuple.primitive.DoubleDoublePair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

public class BiasOptimizerTest {
    @Test
    public void testManual() {
        long[] occCounts = {1, 2, 3, 5};
        long[] occCountFreqs = {50, 30, 10, 10};
        SegmentCCDF ccdf = new SegmentCCDF(
                occCounts,
                occCountFreqs
        );
        FastList<SegmentCCDF> segments = new FastList<>();
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

        double[] xLoc = new double[nSegments];
        for (int i = 0; i <nSegments; i++) {
            xLoc[i] = 2.0;
        }
        xLoc[0] = 1.0;

        f.compute(xLoc);
        double fval = f.getValue();
        double[] fgrad = f.getGradient();

        double gradDelta = 0.001;
        xLoc[0] += gradDelta;
        f.compute(xLoc);
        double fval2 = f.getValue();
        assertEquals((fval2-fval)/gradDelta, fgrad[0], 1);

        BFGSOptimizer opt = new BFGSOptimizer(f);
        opt.setVerbose(false);
        double[] xSolve = new double[nSegments];
        xSolve = opt.solve(xSolve, 1e-5);
        assertTrue(xSolve[0] > 0);
        for(int i = 0; i < nSegments; i++) {
            xSolve[i] = FastMath.exp(xSolve[i]);
        }

        LongArrayList xs = new LongArrayList();
        int newItem = 0;
        for (int i = 0; i < occCounts.length; i++) {
            for (int j = 0; j < occCountFreqs[i]; j++) {
                for (int k = 0; k < occCounts[i]; k++) {
                    xs.add(newItem);
                }
                newItem++;
            }
        }
        FastList<LongList> rawSegments = new FastList<>(nSegments);
        for (int i = 0; i < nSegments; i++) {
            rawSegments.add(xs);
        }
        BiasOptimizer<LongList> bopt = new FreqBiasOptimizer();
        bopt.compute(segSpace, rawSegments);
        double[] xSolve2 = bopt.getBias();
        assertArrayEquals(xSolve, xSolve2, 1e-2);
    }
}