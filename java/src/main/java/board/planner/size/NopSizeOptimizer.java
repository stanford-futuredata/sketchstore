package board.planner.size;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Arrays;

public class NopSizeOptimizer<TL extends PrimitiveIterable> implements SizeOptimizer<TL> {
    double[] scalings;

    @Override
    public int[] getSizes(int totalSize) {
        int[] scaledSizes = new int[scalings.length];
        for (int i = 0; i < scalings.length; i++) {
            scaledSizes[i] = (int)(totalSize * scalings[i]);
        }
        return scaledSizes;
    }

    @Override
    public void compute(LongList segmentSizes, FastList<LongList> segmentDimensions, double workloadProb) {
        int nSeg = segmentSizes.size();
        scalings = new double[nSeg];
        Arrays.fill(scalings, 1.0/nSeg);
    }
}
