package summary.compressor.freq;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import summary.CounterLongSketch;
import summary.accumulator.ArgSort;

public class CoopFreqCompressor implements ItemCounterCompressor {
    LongDoubleHashMap deltas;
    int interval_len;
    int cur_idx;

    public CoopFreqCompressor(int interval_len) {
        this.interval_len = interval_len;
        cur_idx = 0;
        deltas = new LongDoubleHashMap();
    }

    public void reset() {
        deltas.clear();
        cur_idx = 0;
    }

    @Override
    public CounterLongSketch compress(LongDoubleHashMap xs, int size) {
        DoubleList counts = xs.values().toSortedList().reverseThis();
        double t = ItemCountsUtil.find_t(counts, size);
        LongArrayList itemsToStore = new LongArrayList(size);
        DoubleArrayList weightsToStore = new DoubleArrayList(size);

        DoubleArrayList deltaList = new DoubleArrayList(xs.size());
        LongArrayList xList = new LongArrayList(xs.size());
        xs.forEachKeyValue((long curX, double curVal) -> {
            if (curVal >= t) {
                itemsToStore.add(curX);
                weightsToStore.add(curVal);
            } else {
                deltas.addToValue(curX, curVal);
                deltaList.add(-deltas.get(curX));
                xList.add(curX);
            }
        });
        int[] deltaOrdering = ArgSort.argSort(deltaList);
        for (int orderedIdx : deltaOrdering) {
            if (itemsToStore.size() >= size) {
                break;
            }
            long curX = xList.get(orderedIdx);
            double amtToStore = -deltaList.get(orderedIdx);
            if (amtToStore > t) {
                amtToStore = t;
            }
            assert (amtToStore >= 0);
            itemsToStore.add(curX);
            weightsToStore.add(amtToStore);
            deltas.addToValue(curX, -amtToStore);
        }

        cur_idx += 1;
        if (interval_len > 0 && cur_idx == interval_len) {
            reset();
        }

        return new CounterLongSketch(itemsToStore.toArray(), weightsToStore.toArray());
    }
}
