package board.planner.size;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

public class CoopSizeOptimizer<TL extends PrimitiveIterable> implements SizeOptimizer<TL> {
    double scalePower;
    double[] scalings;

    public CoopSizeOptimizer(
            double scalePower
    ) {
        this.scalePower = scalePower;
    }
    public CoopSizeOptimizer() {
        this.scalePower = 1.0/3;
    }

    @Override
    public void compute(
            LongList segmentSizes, FastList<LongList> segmentDimensions, double workloadProb
    ) {
        int nSeg = segmentSizes.size();
        int nDim = segmentDimensions.get(0).size();
        MutableMap<LongList, Double> segmentA2s = new UnifiedMap<>(nSeg);
        long[] dimensionCardinalities = new long[nDim];
        for (LongList curSeg : segmentDimensions) {
            segmentA2s.put(curSeg, 0.0);
            for (int j = 0; j < nDim; j++) {
                if (curSeg.get(j) >= dimensionCardinalities[j]) {
                    dimensionCardinalities[j] = curSeg.get(j)+1;
                }
            }
        }

        int[] filterOnDimFlags = new int[nDim];
        int maxSets = (int) FastMath.pow(2.0, nDim);
        for (int comboIdx = 0; comboIdx < maxSets; comboIdx++) {
//            System.out.println(Arrays.toString(filterOnDimFlags));

            int numFilterDims = 0;
            double comboProbability = 1.0;
            for (int j = 0; j < nDim; j++) {
                if (filterOnDimFlags[j] > 0) {
                    numFilterDims++;
                    comboProbability *= (workloadProb/dimensionCardinalities[j]);
                } else {
                    comboProbability *= (1-workloadProb);
                }
            }

            MutableMap<LongArrayList, Double> groupSizes = new UnifiedMap<>();
            FastList<LongArrayList> groupKeys = new FastList<>(nSeg);
            for (int i = 0; i < nSeg; i++) {
                LongList curSegment = segmentDimensions.get(i);
                LongArrayList groupKey = getGroupKey(curSegment, filterOnDimFlags, numFilterDims);
                groupKeys.add(groupKey);
                double curWeight = groupSizes.getOrDefault(groupKey, 0.0);
                groupSizes.put(groupKey, curWeight+segmentSizes.get(i));
            }
            for (int i = 0; i < nSeg; i++) {
                LongList curSegment = segmentDimensions.get(i);
                LongArrayList groupKey = groupKeys.get(i);
                double curSegmentA2 = segmentA2s.get(curSegment);
                double curGroupSize = groupSizes.get(groupKey);
                segmentA2s.put(curSegment, curSegmentA2 + comboProbability/(curGroupSize*curGroupSize));
            }

            int carry = 1;
            for (int j = 0; j < nDim; j++) {
                filterOnDimFlags[j] += carry;
                if (filterOnDimFlags[j] > 1) {
                    carry = filterOnDimFlags[j]/2;
                    filterOnDimFlags[j] -= carry*2;
                } else {
                    break;
                }
            }
        }

        scalings = new double[nSeg];
        double totalSize = 0;
        for (int i = 0; i < nSeg; i++) {
            double curSegmentSize = segmentSizes.get(i);
            scalings[i] = FastMath.pow(
                    segmentA2s.get(segmentDimensions.get(i)) * curSegmentSize*curSegmentSize,
                    scalePower
            );
            totalSize += scalings[i];
        }
        for (int i = 0; i < nSeg; i++) {
            scalings[i] /= totalSize;
        }
    }
    public static LongArrayList getGroupKey(LongList segDimensions, int[] filterOnDimFlags, int numFilterDims) {
        LongArrayList groupKey = new LongArrayList(numFilterDims);
        for (int j = 0; j < filterOnDimFlags.length; j++) {
            if (filterOnDimFlags[j] > 0) {
                groupKey.add(segDimensions.get(j));
            }
        }
        return groupKey;
    }

    @Override
    public double[] getScaling() {
        return scalings;
    }
}
