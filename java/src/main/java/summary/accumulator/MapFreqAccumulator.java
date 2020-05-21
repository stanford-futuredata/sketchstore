package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;
import summary.DictSketch;

import java.util.List;

public class MapFreqAccumulator implements Accumulator<Long, LongList> {
    public LongDoubleHashMap values;
    public MapFreqAccumulator() {
        values = new LongDoubleHashMap();
    }
    public MapFreqAccumulator(int size) {
        values = new LongDoubleHashMap(size);
    }

    @Override
    public void reset() {
        values.clear();
    }

    @Override
    public int compress(int size) {
        return 0;
    }

    @Override
    public void addRaw(LongList xs) {
        for (int i = 0; i < xs.size(); i++) {
            values.addToValue(xs.get(i), 1.0);
        }
    }

    @Override
    public void addSketch(Object curObject) {
        if (curObject instanceof DictSketch) {
            DictSketch curSketch = (DictSketch) curObject;
            curSketch.vals.forEachKeyValue(this::addTovalue);
        } else if (curObject instanceof CounterLongSketch) {
            CounterLongSketch curSketch = (CounterLongSketch) curObject;
            int n = curSketch.vals.length;
            long[] vals = curSketch.vals;
            double[] weights = curSketch.weights;
            for (int i = 0; i < n; i++) {
                values.addToValue(vals[i], weights[i]);
            }
        } else {
            throw new RuntimeException("Invalid sketch type: "+curObject.getClass().getCanonicalName());
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

    private void addTovalue(long x, double v) {
        values.addToValue(x, v);
    }
}
