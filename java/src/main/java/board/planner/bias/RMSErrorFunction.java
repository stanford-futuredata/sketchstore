package board.planner.bias;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.tuple.primitive.DoubleDoublePair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

public class RMSErrorFunction implements FunctionWithGrad {
    double[] grad;
    double value;
    FastList<SegmentCCDF> segments;
    int[] segmentSpaces;

    public RMSErrorFunction(
            FastList<SegmentCCDF> segments,
            int[] segmentSpaces
    ) {
        int nSegs = segments.size();
        this.segments = segments;
        this.segmentSpaces = segmentSpaces;
        grad = new double[nSegs];
        value = 0;
    }

    public static class SegmentCCDF {
        long[] occCounts;
        // total weight of items that occur less than a count # of times
        long[] occCountFrequency;
        public SegmentCCDF(long[] occCounts, long[] occCountFrequency) {
            this.occCounts = occCounts;
            this.occCountFrequency = occCountFrequency;
        }
        public static <TL extends PrimitiveIterable> SegmentCCDF fromItems(TL x) {
            long[] curCounts;
            long[] curCountFrequencies;
            return null;
        }
        public DoubleDoublePair total(double bias) {
            int n = occCounts.length;
            double adjTotal = 0;
            double adjTotalDeriv = 0;
            for (int i = 0;i < n; i++) {
                if (occCounts[i] > bias) {
                    adjTotal += (occCounts[i]-bias) * occCountFrequency[i];
                    adjTotalDeriv -= occCountFrequency[i];
                }
            }
            return PrimitiveTuples.pair(adjTotal, adjTotalDeriv);
        }
    }

    @Override
    public void compute(double[] x) {
        int nSeg = segments.size();
        double biasTerm = 0;
        double varTerm = 0;
        for (int i = 0; i < nSeg; i++) {
            SegmentCCDF segCDF = segments.get(i);
            DoubleDoublePair p = segCDF.total(x[i]);
            double ni = p.getOne();
            double dni = p.getTwo();

            biasTerm += x[i];
            double scaledTotal = ni/segmentSpaces[i];
            varTerm += .25*scaledTotal*scaledTotal;
            grad[i] = 2*x[i] + .5*scaledTotal*dni/segmentSpaces[i];
        }
        value = biasTerm*biasTerm + varTerm;
    }

    @Override
    public int dim() {
        return segmentSpaces.length;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public double[] getGradient() {
        return grad;
    }
}
