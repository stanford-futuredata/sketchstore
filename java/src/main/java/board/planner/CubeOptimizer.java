package board.planner;

import board.planner.bias.BiasOptimizer;
import board.planner.size.SizeOptimizer;
import board.planner.size.SizeUtils;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.Map;

public class CubeOptimizer<TL extends PrimitiveIterable> implements PlanOptimizer<TL> {
    public IntArrayList segmentSpaces;
    public LongArrayList segmentBiases;

    public BiasOptimizer<TL> biasOptimizer;
    public SizeOptimizer<TL> sizeOptimizer;
    double workloadProb;

    @Override
    public IntList getSpaces() {
        return segmentSpaces;
    }

    @Override
    public LongList getBiases() {
        return segmentBiases;
    }

    public CubeOptimizer(
        SizeOptimizer<TL> sizeOptimizer,
        BiasOptimizer<TL> biasOptimizer
    ) {
        this.sizeOptimizer = sizeOptimizer;
        this.biasOptimizer = biasOptimizer;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        workloadProb = (Double) params.get("workload_prob");
    }

    @Override
    public void optimizePlan(FastList<TL> segments, FastList<LongList> segDimensions, int size) {
        int numSegments = segments.size();
        LongArrayList segmentSizes = new LongArrayList(numSegments);
        for (TL curSegment : segments) {
            segmentSizes.add(curSegment.size());
        }

        sizeOptimizer.compute(
                segmentSizes,
                segDimensions,
                workloadProb
        );
        int[] segmentSpaceArray = sizeOptimizer.getSizes(size);
        segmentSpaces = new IntArrayList(segmentSpaceArray);

        biasOptimizer.compute(
                segmentSpaceArray,
                segments
        );
        double[] biasArray = biasOptimizer.getBias();
        long[] roundedBiasArray = SizeUtils.safeRoundLong(biasArray);
        segmentBiases = new LongArrayList(roundedBiasArray);
    }
}
