package summary.compressor.freq;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;

import java.util.Random;

public class HaircombCompressor implements ItemCounterCompressor {
    Random random;
    public HaircombCompressor(int seed) {
        random = new Random(seed);
    }

    @Override
    public CounterLongSketch compress(LongDoubleHashMap xs, int size) {
        DoubleList counts = xs.values().toSortedList().reverseThis();
        double t = ItemCountsUtil.find_t(counts, size);
//        LongDoubleHashMap itemsToStore = new LongDoubleHashMap(size);
        LongArrayList itemsToStore = new LongArrayList(size);
        DoubleArrayList weightsToStore = new DoubleArrayList(size);

        int n = xs.size();
        LongArrayList remainingItems = new LongArrayList(n);
        DoubleArrayList remainingWeights = new DoubleArrayList(n);
        xs.forEachKeyValue((long k, double v) -> {
            if (v >= t) {
                itemsToStore.add(k);
                weightsToStore.add(v);
            } else {
                remainingItems.add(k);
                remainingWeights.add(v);
            }
        });

        int nRemaining = remainingItems.size();
        double randShift = random.nextDouble()*t;
        double runningSum = 0.0;
        for (int i = 0; i < nRemaining; i++) {
            long curItem = remainingItems.get(i);
            double curWeight = remainingWeights.get(i);
            runningSum += curWeight;
            if (runningSum > randShift) {
                runningSum -= t;
                itemsToStore.add(curItem);
                weightsToStore.add(t);
            }
        }

        return new CounterLongSketch(itemsToStore.toArray(), weightsToStore.toArray());
    }
}
