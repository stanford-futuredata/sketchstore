package board.planner.size;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Arrays;

public class PropSizeOptimizer<TL extends PrimitiveIterable> implements SizeOptimizer<TL> {
    double[] scalings;
    int minSize = 1;

    @Override
    public int[] getSizes(int totalSize) {
        return SizeUtils.safeScaleWithMin(scalings, totalSize, minSize);
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
