package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleDoubleHashMap;
import summary.CounterDoubleSketch;
import summary.Sketch;

import java.util.Arrays;
import java.util.List;

public class ListQuantileAccumulator implements Accumulator<Double, DoubleList>{
    public DoubleArrayList items;
    public DoubleArrayList weights;

    public ListQuantileAccumulator() {
        items = new DoubleArrayList();
        weights = new DoubleArrayList();
    }

    @Override
    public void reset() {
        items.clear();
        weights.clear();
    }

    @Override
    public int compress(int size) {
        return 0;
    }

    @Override
    public void addRaw(DoubleList xs) {
        items.addAll(xs);
        int n = xs.size();
        for (int i = 0; i < n; i++) {
            weights.add(1.0);
        }
    }

    @Override
    public void addSketch(Sketch<Double> newObject) {
        assert(newObject instanceof CounterDoubleSketch);
        CounterDoubleSketch newSketch = (CounterDoubleSketch) newObject;
        items.addAll(newSketch.values);
        weights.addAll(newSketch.weights);
    }

    public void reIndex() {
        int[] sortedIdxs = ArgSort.argSort(items);
        int n = items.size();
        DoubleArrayList newItems = new DoubleArrayList(n);
        DoubleArrayList newWeights = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            int curIdx = sortedIdxs[i];
            if (i > 0 && items.get(curIdx) == items.get(sortedIdxs[i-1])) {
                int lastNewIdx = newWeights.size()-1;
                newWeights.set(lastNewIdx, newWeights.get(lastNewIdx)+weights.get(curIdx));
            } else {
                newItems.add(items.get(curIdx));
                newWeights.add(weights.get(curIdx));
            }
        }
        items = newItems;
        weights = newWeights;
    }

    @Override
    public DoubleList estimate(List<Double> xToTrack) {
        reIndex();

        int nToTrack = xToTrack.size();
        int nStored = items.size();

        DoubleArrayList xRanks = new DoubleArrayList(nToTrack);
        double curRank = 0.0;

        int itemIdx = 0;
        for (double x : xToTrack) {
            while (itemIdx < nStored && items.get(itemIdx) <= x) {
                curRank += weights.get(itemIdx);
                itemIdx++;
            }
            xRanks.add(curRank);
        }
        return xRanks;
    }
}
