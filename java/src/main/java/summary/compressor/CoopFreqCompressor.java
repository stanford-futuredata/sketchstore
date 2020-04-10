package summary.compressor;

import org.eclipse.collections.api.DoubleIterable;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.ordered.primitive.OrderedDoubleIterable;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;

public class CoopFreqCompressor implements ItemDictCompressor{
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
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        DoubleList counts = xs.values().toSortedList().reverseThis();
        double t = ItemCountsUtil.find_t(counts, size);
        LongDoubleHashMap itemsToStore = new LongDoubleHashMap(size);

        for (LongDoublePair xv : xs.keyValuesView()) {
            long curX = xv.getOne();
            double curVal = xv.getTwo();
            if (curVal >= t) {
                itemsToStore.put(curX, curVal);
            } else {
                deltas.addToValue(curX, curVal);
            }
        }
        MutableList<LongDoublePair> orderedDeficit = xs.keysView(
        ).collect(
                (long x) -> PrimitiveTuples.pair(x, deltas.get(x))
        ).toSortedListBy((LongDoublePair xv) -> -xv.getTwo());

        for (LongDoublePair xv : orderedDeficit) {
            long curX = xv.getOne();
            if (itemsToStore.size() >= size) {
                break;
            } else if (!itemsToStore.containsKey(curX)) {
                double amtToStore = xv.getTwo();
                if (amtToStore > t) {
                    amtToStore = t;
                }
                assert(amtToStore >= 0);
                itemsToStore.put(curX, amtToStore);
                deltas.addToValue(curX, -amtToStore);
            }
        }

        cur_idx += 1;
        if (interval_len > 0 && cur_idx == interval_len) {
            reset();
        }

        return itemsToStore;
    }
}
