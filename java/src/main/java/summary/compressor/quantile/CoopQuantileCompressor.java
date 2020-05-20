package summary.compressor.quantile;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CDFSketch;
import summary.accumulator.SortedQuantileAccumulator;

public class CoopQuantileCompressor implements SeqDictCompressor {
    public SortedQuantileAccumulator trueCDF;
    public SortedQuantileAccumulator storedCDF;

    public CoopQuantileCompressor() {
        trueCDF = new SortedQuantileAccumulator();
        storedCDF = new SortedQuantileAccumulator();
    }

    @Override
    public CDFSketch compress(DoubleArrayList xs, int size) {
        trueCDF.add(xs);
        CDFSketch xDeltas = trueCDF.calcDelta(storedCDF);

        DoubleArrayList savedItems = new DoubleArrayList(size);
        DoubleArrayList savedWeights = new DoubleArrayList(size);

        int n = xs.size();
        int skip = (int)Math.ceil(n*1.0/size);
        int startIdx = 0;
        while (startIdx < n) {
            int endIdx = startIdx + skip;
            if (endIdx > n) {
                endIdx = n;
            }
            int segmentSize = endIdx - startIdx;

            double leftValue = xs.get(startIdx);
            double rightValue = xs.get(endIdx-1);
            int deltaStartIdx = trueCDF.searchItemIdx(leftValue);
            int deltaEndIdx = trueCDF.searchItemIdx(rightValue)+1;
            int storedIdx = findOptimalStore(
                    xDeltas.cumTotal,
                    segmentSize,
                    deltaStartIdx,
                    deltaEndIdx
            );
            double toSave = xDeltas.values.get(storedIdx);
            for (int i = storedIdx; i < n; i++) {
                xDeltas.cumTotal.set(i, xDeltas.cumTotal.get(i) - segmentSize);
            }

            int numDistinctSaved = savedItems.size();
            if (!savedItems.isEmpty() && savedItems.getLast() == toSave) {
                double existingWeight = savedWeights.get(numDistinctSaved-1);
                savedWeights.set(numDistinctSaved-1, existingWeight+segmentSize);
            } else {
                savedItems.add(toSave);
                savedWeights.add(segmentSize);
            }
            startIdx += skip;
        }

        storedCDF.add(savedItems, savedWeights);
        return CDFSketch.fromWeights(savedItems, savedWeights);
    }

    public int findOptimalStore(
            DoubleArrayList deltaCDF,
            double curSegWeight,
            int startIdx,
            int endIdx
    ) {
        double a = 1.0/Math.sqrt(1024);
        double scaleFactor = a / curSegWeight;

        int bestIdxToStore = -1;
        double bestCumLossDelta = Double.MAX_VALUE;
        double suffixCumLossDelta = 0.0;
        for (int curIdx = endIdx-1; curIdx >= startIdx; curIdx--) {
            double lossIfStored = FastMath.cosh(
                    (deltaCDF.get(curIdx) - curSegWeight)*scaleFactor
            );
            double lossCurrent = FastMath.cosh(
                    (deltaCDF.get(curIdx))*scaleFactor
            );
            suffixCumLossDelta += (lossCurrent - lossIfStored);
            if (suffixCumLossDelta < bestCumLossDelta) {
                bestIdxToStore = curIdx;
                bestCumLossDelta = suffixCumLossDelta;
            }
        }
        return bestIdxToStore;
    }
}
