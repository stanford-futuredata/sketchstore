package board.planner.bias;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

public class FreqBiasOptimizer implements BiasOptimizer<LongList> {
    double[] biasValues;

    @Override
    public void compute(int[] segmentSpaces, FastList<LongList> segmentValues) {
        int nSegments = segmentValues.size();
        FastList<SegmentCCDF> segmentCCDFs = new FastList<>(nSegments);
        for (LongList curValues : segmentValues) {
            segmentCCDFs.add(
                    SegmentCCDF.fromItems(curValues)
            );
        }
        RMSErrorFunction f = new RMSErrorFunction(
                segmentCCDFs,
                segmentSpaces
        );
        BFGSOptimizer opt = new BFGSOptimizer(f);
        opt.setMaxIter(20);
        opt.setVerbose(true);
        double[] xSolve = new double[nSegments];
        biasValues = opt.solve(xSolve, 1e-5);
    }

    @Override
    public double[] getBias() {
        return biasValues;
    }
}
