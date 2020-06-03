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
        int nSegments = scalings.length;
        double[] scaledSizes = new double[nSegments];
        totalSize -= minSize*nSegments;

        for (int i = 0; i < scalings.length; i++) {
            scaledSizes[i] = scalings[i] * totalSize;
        }
        int[] finalSize = SizeUtils.safeRound(scaledSizes);
        for (int i = 0; i < nSegments; i++){
            finalSize[i] += minSize;
        }
        return finalSize;
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
