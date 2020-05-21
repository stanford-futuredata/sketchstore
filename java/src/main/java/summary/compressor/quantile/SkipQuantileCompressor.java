package summary.compressor.quantile;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CounterDoubleSketch;

import java.util.Random;

public class SkipQuantileCompressor implements SeqCDFCompressor {
    boolean isRandom;
    Random rng;

    public SkipQuantileCompressor(boolean isRandom, int seed) {
        this.isRandom = isRandom;
        rng = new Random(seed);
    }

    @Override
    public CounterDoubleSketch compress(DoubleList xs, int size) {
        int n = xs.size();
        int skip = (int)Math.ceil(n*1.0/size);
        DoubleArrayList savedItems = new DoubleArrayList(size);
        DoubleArrayList savedWeights = new DoubleArrayList(size);

        int startIdx = 0;
        while (startIdx < n) {
            int endIdx = startIdx + skip;
            if (endIdx > n) {
                endIdx = n;
            }

            int segmentSize = endIdx - startIdx;
            int segmentOffset = -1;
            if (isRandom) {
                segmentOffset = rng.nextInt(segmentSize);
            } else {
                segmentOffset = segmentSize / 2;
            }
            double toSave = xs.get(startIdx + segmentOffset);

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

        return new CounterDoubleSketch(
                savedItems.toArray(),
                savedWeights.toArray()
        );
    }
}
