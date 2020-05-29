package board.planner.bias;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import runner.factory.FreqSketchGenFactory;

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
        opt.setMaxIter(15);
        opt.setVerbose(false);
        biasValues = new double[nSegments];
        biasValues= opt.solve(biasValues, 1e-4);
        for (int i = 0; i < nSegments; i++) {
            biasValues[i] = FastMath.exp(biasValues[i]);
        }
    }

    @Override
    public double[] getBias() {
        return biasValues;
    }
}
