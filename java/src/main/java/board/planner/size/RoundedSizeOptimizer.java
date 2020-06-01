package board.planner.size;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Arrays;

public class RoundedSizeOptimizer<TL extends PrimitiveIterable> implements SizeOptimizer<TL> {
    int nSeg;
    double rawValue = 0.0;

    @Override
    public int[] getSizes(int totalSize) {
        double[] scaledSizes = new double[nSeg];
        for (int i = 0; i < nSeg; i++) {
            scaledSizes[i] = rawValue * totalSize;
        }
        return SizeUtils.safeRound(scaledSizes);
    }

    @Override
    public void compute(LongList segmentSizes, FastList<LongList> segmentDimensions, double workloadProb) {
        nSeg = segmentSizes.size();
        rawValue = 1.0/nSeg;
    }
}
