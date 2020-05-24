package summary.compressor.quantile;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CounterDoubleSketch;
import summary.accumulator.SortedQuantileAccumulator;

public class CoopQuantileCompressor implements SeqCounterCompressor {
    public SortedQuantileAccumulator trueCDF;
    public SortedQuantileAccumulator storedCDF;
    public int maxAccSize = -1;

    public CoopQuantileCompressor() {
        trueCDF = new SortedQuantileAccumulator();
        storedCDF = new SortedQuantileAccumulator();
    }

    public void setMaxAccSize(int maxAccSize) {
        this.maxAccSize = maxAccSize;
    }

    @Override
    public CounterDoubleSketch compress(DoubleList xs, int size) {
        trueCDF.addRaw(xs);
        DoubleList deltaItems = trueCDF.items;
        double[] xDeltas = trueCDF.calcDelta(storedCDF);

        DoubleArrayList savedItems = new DoubleArrayList(size);
        DoubleArrayList savedWeights = new DoubleArrayList(size);

        int n = xs.size();
        int nDelta = xDeltas.length;
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
                    xDeltas,
                    segmentSize,
                    deltaStartIdx,
                    deltaEndIdx
            );
            double toSave = deltaItems.get(storedIdx);
            for (int i = storedIdx; i < nDelta; i++) {
                xDeltas[i] -= segmentSize;
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

        if (maxAccSize > 0 && trueCDF.size() > maxAccSize) {
            trueCDF.compress(maxAccSize/2);
        }
        if (maxAccSize > 0 && storedCDF.size() > maxAccSize) {
            storedCDF.compress(maxAccSize/2);
        }
        return new CounterDoubleSketch(savedItems.toArray(), savedWeights.toArray());
    }

    /**
     * Fast Taylor expansion of cosh
     * @param x argument
     * @return approximate cosh
     */
    public static double fastcosh(double x) {
        double x2 = x*x;
        return 1 + x2/2 + x2*x2/24;
    }

    public int findOptimalStore(
            double[] deltaCDF,
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
            double deltaIfStored = (deltaCDF[curIdx] - curSegWeight)*scaleFactor;
            double deltaCurrent = (deltaCDF[curIdx])*scaleFactor;
            double lossDelta = fastcosh(deltaIfStored) - fastcosh(deltaCurrent);
//            double lossIfStored = Math.cosh(
//                    (deltaCDF[curIdx] - curSegWeight)*scaleFactor
//            );
//            double lossCurrent = Math.cosh(
//                    (deltaCDF[curIdx])*scaleFactor
//            );
//            suffixCumLossDelta += (lossIfStored - lossCurrent);
            suffixCumLossDelta += lossDelta;
            if (suffixCumLossDelta < bestCumLossDelta) {
                bestIdxToStore = curIdx;
                bestCumLossDelta = suffixCumLossDelta;
            }
        }
        return bestIdxToStore;
    }
}
