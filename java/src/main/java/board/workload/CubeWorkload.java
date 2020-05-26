package board.workload;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.Random;

public class CubeWorkload {
    Random random;

    public CubeWorkload(
            int seed
    ) {
        random = new Random(seed);
    }

    public FastList<LongList> generate(
            LongList dimensionCardinalities,
            double workloadProbability,
            int numQueries
    ) {
        int nDims = dimensionCardinalities.size();
        FastList<LongList> dimensionConditions = new FastList<>(numQueries);
        for (int queryIdx = 0; queryIdx < numQueries; queryIdx++) {
            LongArrayList curDimensions = new LongArrayList(nDims);
            for (int j = 0; j < nDims; j++) {
                boolean includeDimension = (random.nextDouble() < workloadProbability);
                if (includeDimension) {
                    curDimensions.add(random.nextInt((int)dimensionCardinalities.get(j)));
                } else {
                    curDimensions.add(-1);
                }
            }
            dimensionConditions.add(curDimensions);
        }
        return dimensionConditions;
    }
}
