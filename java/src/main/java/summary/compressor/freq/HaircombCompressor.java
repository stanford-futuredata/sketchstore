package summary.compressor.freq;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

import java.util.Random;

public class HaircombCompressor implements ItemDictCompressor {
    Random random;
    public HaircombCompressor(int seed) {
        random = new Random(seed);
    }

    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        DoubleList counts = xs.values().toSortedList().reverseThis();
        double t = ItemCountsUtil.find_t(counts, size);
        LongDoubleHashMap itemsToStore = new LongDoubleHashMap(size);

        int n = xs.size();
        LongArrayList remainingItems = new LongArrayList(n);
        DoubleArrayList remainingWeights = new DoubleArrayList(n);
        xs.forEachKeyValue((long k, double v) -> {
            if (v >= t) {
                itemsToStore.put(k, v);
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
                itemsToStore.put(curItem, t);
            }
        }

        return itemsToStore;
    }
}
