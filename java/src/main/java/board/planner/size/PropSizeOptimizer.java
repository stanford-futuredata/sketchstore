package board.planner.size;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Arrays;

public class PropSizeOptimizer<TL extends PrimitiveIterable> implements SizeOptimizer<TL> {
    double[] scalings;

    @Override
    public int[] getSizes(int totalSize) {
        double[] scaledSizes = new double[scalings.length];
        for (int i = 0; i < scalings.length; i++) {
            scaledSizes[i] = scalings[i] * totalSize;
        }
        return SizeUtils.safeRound(scaledSizes);
    }

    @Override
    public void compute(LongList segmentSizes, FastList<LongList> segmentDimensions, double workloadProb) {
        int nSeg = segmentSizes.size();
        double totalSizes = segmentSizes.sum();
        scalings = new double[nSeg];
        for (int i = 0; i < nSeg; i++) {
            scalings[i] = segmentSizes.get(i) / totalSizes;
        }
    }
}
