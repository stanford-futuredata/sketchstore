package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.DictSketch;

import java.util.List;

public class ExactFreqAccumulator implements FreqAccumulator {
    public LongDoubleHashMap values;
    public ExactFreqAccumulator() {
        values = new LongDoubleHashMap();
    }
    public ExactFreqAccumulator(int size) {
        values = new LongDoubleHashMap(size);
    }

    @Override
    public void reset() {
        values.clear();
    }

    @Override
    public void add(LongList xs) {
        for (int i = 0; i < xs.size(); i++) {
            values.addToValue(xs.get(i), 1.0);
        }
    }
    @Override
    public void add(LongDoubleHashMap curMap) {
        for (LongDoublePair xv : curMap.keyValuesView()) {
            values.addToValue(xv.getOne(), xv.getTwo());
        }
    }

    @Override
    public DoubleList estimate(List<Long> xToTrack) {
        DoubleArrayList xCounts = new DoubleArrayList(xToTrack.size());
        int size = xToTrack.size();
        for (int i = 0; i < size; i++) {
            long aLong = xToTrack.get(i);
            xCounts.add(values.getIfAbsent(aLong, 0));
        }
        return xCounts;
    }
}
