package board.planner;

import board.planner.bias.BiasOptimizer;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;

import java.util.Arrays;
import java.util.Map;

public class CubeOptimizer<TL extends PrimitiveIterable> implements PlanOptimizer<TL> {
    public int totalSize;
    public IntArrayList segmentSpaces;
    public LongArrayList segmentBiases;
    public double workloadProb;

    public BiasOptimizer<TL> biasOptimizer;
    boolean optimizeSpace = false;


    @Override
    public IntList getSpaces() {
        return segmentSpaces;
    }

    @Override
    public LongList getBiases() {
        return segmentBiases;
    }

    public CubeOptimizer(
        BiasOptimizer<TL> biasOptimizer
    ) {
        this.biasOptimizer = biasOptimizer;
    }

    @Override
    public void setParams(Map<String, Object> params) {
        this.workloadProb = (Double) params.get("workload_prob");
    }

    @Override
    public void optimizePlan(FastList<TL> segments, FastList<LongList> segDimensions, int size) {
        this.totalSize = size;
        int numSegments = segments.size();
        LongArrayList segmentSizes = new LongArrayList(numSegments);
        for (TL curSegment : segments) {
            segmentSizes.add(curSegment.size());
        }

        double[] scaledSizes;
        if (optimizeSpace) {
            SizePlanner sizeOpt = new SizePlanner(
                    segmentSizes,
                    segDimensions,
                    workloadProb
            );
            scaledSizes = sizeOpt.calcScaling();
        } else {
            scaledSizes = new double[numSegments];
            Arrays.fill(scaledSizes, 1.0/numSegments);
        }
        for (int i = 0; i < scaledSizes.length; i++) {
            scaledSizes[i] *= size;
        }
        int[] roundedSegmentSizes = SizeUtils.safeRound(scaledSizes);
        segmentSpaces = new IntArrayList(roundedSegmentSizes);

        biasOptimizer.compute(
                roundedSegmentSizes,
                segments
        );
        double[] biasArray = biasOptimizer.getBias();
        long[] roundedBiasArray = SizeUtils.safeRoundLong(biasArray);
        segmentBiases = new LongArrayList(roundedBiasArray);
    }

    public void setOptimizeSpace(boolean flag) {
        optimizeSpace = flag;
    }
}
