package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleDoubleHashMap;
import summary.CounterDoubleSketch;
import summary.Sketch;

import java.util.List;

public class MapQuantileAccumulator implements Accumulator<Double, DoubleList>{
    public DoubleDoubleHashMap itemWeights;
    public double[] sortedItems;

    public MapQuantileAccumulator() {
        itemWeights = new DoubleDoubleHashMap();
    }

    @Override
    public void reset() {
        itemWeights.clear();
    }

    @Override
    public int compress(int size) {
        return 0;
    }

    @Override
    public void addRaw(DoubleList xs) {
        int nSegment = xs.size();
        for (int i = 0; i < nSegment; i++) {
            double x = xs.get(i);
            itemWeights.addToValue(x, 1.0);
        }
        invalidateIndex();
    }

    @Override
    public void addSketch(Sketch<Double> newObject) {
        assert(newObject instanceof CounterDoubleSketch);
        CounterDoubleSketch newSketch = (CounterDoubleSketch) newObject;
        int nSketch = newSketch.values.length;
        double[] values = newSketch.values;
        double[] weights = newSketch.weights;
        for (int i = 0; i < nSketch; i++) {
            itemWeights.addToValue(values[i], weights[i]);
        }
        invalidateIndex();
    }

    @Override
    public DoubleList estimate(List<Double> xToTrack) {
        if (sortedItems == null) {
            reIndex();
        }
        int n = xToTrack.size();
        int nStored = sortedItems.length;
        DoubleArrayList xRanks = new DoubleArrayList(n);
        double curRank = 0.0;

        int itemIdx = 0;
        for (double x : xToTrack) {
            while (itemIdx < nStored && sortedItems[itemIdx] <= x) {
                curRank += itemWeights.get(sortedItems[itemIdx]);
                itemIdx++;
            }
            xRanks.add(curRank);
        }
        return xRanks;
    }

    public void reIndex() {
        sortedItems = itemWeights.keysView().toSortedArray();
    }
    public void invalidateIndex() {
        sortedItems = null;
    }

    private void addItemWeight(double x, double v) {
        itemWeights.addToValue(x, v);
    }
}
