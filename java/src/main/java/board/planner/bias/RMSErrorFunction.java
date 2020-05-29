package board.planner.bias;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.tuple.primitive.DoubleDoublePair;
import org.eclipse.collections.impl.list.mutable.FastList;

public class RMSErrorFunction implements FunctionWithGrad {
    double[] grad;
    double value;
    FastList<SegmentCCDF> segments;
    int[] segmentSpaces;

    double[] xBuffer;

    public RMSErrorFunction(
            FastList<SegmentCCDF> segments,
            int[] segmentSpaces
    ) {
        int nSegs = segments.size();
        this.segments = segments;
        this.segmentSpaces = segmentSpaces;
        grad = new double[nSegs];
        value = 0;

        xBuffer = new double[nSegs];
    }

    @Override
    public void compute(double[] xl) {
        int nSeg = segments.size();
        double biasTerm = 0;
        double varTerm = 0;

        for (int i = 0; i < nSeg; i++) {
            xBuffer[i] = FastMath.exp(xl[i]);
        }

        for (int i = 0; i < nSeg; i++) {
            double x= xBuffer[i];
            SegmentCCDF segCDF = segments.get(i);
            DoubleDoublePair p = segCDF.total(x);
            double ni = p.getOne();
            double dni = p.getTwo();

            biasTerm += x;

            double scaledTotal = ni/segmentSpaces[i];
            varTerm += .25*scaledTotal*scaledTotal;
            grad[i] = .5*scaledTotal*dni/segmentSpaces[i];
        }
        for (int i = 0; i < nSeg; i++) {
            grad[i] += 2*biasTerm;
            grad[i] *= xBuffer[i];
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
