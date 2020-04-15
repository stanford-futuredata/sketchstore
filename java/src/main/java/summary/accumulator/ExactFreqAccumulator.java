package summary.accumulator;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.DictSketch;

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
    public FastList<Double> estimate(FastList<Long> xToTrack) {
        FastList<Double> xCounts = new FastList<>(xToTrack.size());
        int size = xToTrack.size();
        for (int i = 0; i < size; i++) {
            long aLong = xToTrack.get(i);
            xCounts.add(values.getIfAbsent(aLong, 0));
        }
        return xCounts;
    }
}
