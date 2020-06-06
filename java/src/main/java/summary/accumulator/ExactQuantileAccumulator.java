package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CounterDoubleSketch;
import summary.CounterLongSketch;
import summary.Sketch;

import java.util.List;

public class ExactQuantileAccumulator implements Accumulator<Double, DoubleList> {
    public double[] trackedItems;
    public double[] trackedWeights;
    public ExactQuantileAccumulator() {
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
    public void addRaw(DoubleList xs) {
        throw new RuntimeException("Unsupported raw access");
    }

    @Override
    public void addSketch(Sketch<Double> curObject) {
        if (curObject instanceof CounterDoubleSketch) {
            CounterDoubleSketch curSketch = (CounterDoubleSketch) curObject;
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
    public DoubleList estimate(List<Double> xToTrack) {
        int size = xToTrack.size();
        double[] cumWeights = new double[size];
        for (int i = 0; i < size; i++) {
            if (xToTrack.get(i) != trackedItems[i]) {
                throw new RuntimeException("Tracker disagreement");
            }
            if (i > 0) {
                cumWeights[i] = cumWeights[i-1];
            }
            cumWeights[i] += trackedWeights[i];
        }
        return new DoubleArrayList(cumWeights);
    }
}
