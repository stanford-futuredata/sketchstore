package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;
import summary.DictSketch;
import summary.Sketch;

import java.util.Arrays;
import java.util.List;

public class ExactFreqAccumulator implements Accumulator<Long, LongList> {
    public long[] trackedItems;
    public double[] trackedWeights;
    public ExactFreqAccumulator() {
        trackedItems = null;
        trackedWeights = null;
    }

    @Override
    public void reset() {
        trackedItems = null;
        trackedWeights = null;
    }

    @Override
    public int compress(int size) {
        return 0;
    }

    @Override
    public void addRaw(LongList xs) {
        throw new RuntimeException("Unsupported raw access");
    }

    @Override
    public void addSketch(Sketch<Long> curObject) {
        if (curObject instanceof CounterLongSketch) {
            CounterLongSketch curSketch = (CounterLongSketch) curObject;
            int n = curSketch.values.length;
            if (trackedItems == null) {
                trackedItems = curSketch.values;
                trackedWeights = new double[n];
            }
            double[] weights = curSketch.weights;
            for (int i = 0; i < n; i++) {
                trackedWeights[i] += weights[i];
                if (trackedItems[i] != curSketch.values[i]) {
                    throw new RuntimeException("Unequal tracked items");
                }
            }
        } else {
            throw new RuntimeException("Invalid sketch type: "+curObject.getClass().getCanonicalName());
        }
    }

    @Override
    public DoubleList estimate(List<Long> xToTrack) {
        int size = xToTrack.size();
        if (trackedItems == null) {
            trackedWeights = new double[size];
            return new DoubleArrayList(trackedWeights);
        }
        for (int i = 0; i < size; i++) {
            if (xToTrack.get(i) != trackedItems[i]) {
                throw new RuntimeException("Tracker disagreement");
            }
        }
        return new DoubleArrayList(trackedWeights);
    }
}
