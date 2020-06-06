package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;
import summary.DictSketch;
import summary.Sketch;

import java.util.List;

public class MapFreqAccumulator implements Accumulator<Long, LongList> {
    public LongDoubleHashMap values;
    public double floor = 0;
    public MapFreqAccumulator() {
        values = new LongDoubleHashMap();
    }
    public MapFreqAccumulator(int size) {
        values = new LongDoubleHashMap(size);
    }

    @Override
    public void reset() {
        values.clear();
        floor = 0;
    }

    @Override
    public int compress(int size) {
        int curSize = values.size();
        if (curSize <= size) {
            return curSize;
        } else {
            int newSize = (int)(.7 * size);
            double[] sortedWeights = values.toSortedArray();
            double cutoff = sortedWeights[sortedWeights.length - newSize];
            LongDoubleHashMap newValues = new LongDoubleHashMap(newSize);
            int[] sizeCounter = new int[1];
            values.forEachKeyValue((long k, double v) -> {
                if (v >= cutoff &&  sizeCounter[0] < newSize) {
                    newValues.put(k, v-cutoff);
                    sizeCounter[0]++;
                }
            });
            values = newValues;
            floor = floor + cutoff;
            return values.size();
        }
    }

    @Override
    public void addRaw(LongList xs) {
        for (int i = 0; i < xs.size(); i++) {
            values.addToValue(xs.get(i), 1.0);
        }
    }

    @Override
    public void addSketch(Sketch<Long> curObject) {
        if (curObject instanceof DictSketch) {
            DictSketch curSketch = (DictSketch) curObject;
            curSketch.vals.forEachKeyValue(this::addTovalue);
        } else if (curObject instanceof CounterLongSketch) {
            CounterLongSketch curSketch = (CounterLongSketch) curObject;
            int n = curSketch.values.length;
            long[] vals = curSketch.values;
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
            xCounts.add(values.getIfAbsent(aLong, -floor)+floor);
        }
        return xCounts;
    }

    private void addTovalue(long x, double v) {
        values.addToValue(x, v);
    }
}
