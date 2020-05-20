package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CDFSketch;

import java.util.List;

public class SortedQuantileAccumulator implements Accumulator<Double, DoubleList> {
    public DoubleArrayList items;
    public DoubleArrayList weights;
    public SortedQuantileAccumulator() {
        items = new DoubleArrayList();
        weights = new DoubleArrayList();
    }
    public SortedQuantileAccumulator(DoubleArrayList items, DoubleArrayList weights) {
        this.items = items;
        this.weights = weights;
    }

    @Override
    public void addRaw(DoubleList xs) {
        add(xs, DoubleArrayList.newWithNValues(xs.size(), 1.0));
    }

    @Override
    public void addSketch(Object curObject) {
        assert(curObject instanceof CDFSketch);
        CDFSketch sketch = (CDFSketch) curObject;
        int n = sketch.values.size();
        DoubleArrayList weights = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            weights.add(sketch.getWeight(i));
        }
        add(sketch.values, weights);
    }

    @Override
    public DoubleList estimate(List<Double> xToTrack) {
        int n = xToTrack.size();
        int nStored = items.size();
        DoubleArrayList xRanks = new DoubleArrayList(n);
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

    public void add(DoubleList xs, DoubleList segWeights) {
        int n = xs.size();
        int nStored = items.size();
        DoubleArrayList newItems = new DoubleArrayList(nStored + n);
        DoubleArrayList newWeights = new DoubleArrayList(nStored + n);

        int storedIdx = 0;
        int itemIdx = 0;
        while (storedIdx < nStored || itemIdx < n) {
            double nextItem = -1;
            double nextWeight = -1;
            if (storedIdx >= nStored) {
                nextItem = xs.get(itemIdx);
                nextWeight = segWeights.get(itemIdx);
                itemIdx++;
            } else if (itemIdx >= n) {
                nextItem = items.get(storedIdx);
                nextWeight = weights.get(storedIdx);
                storedIdx++;
            } else {
                double x1 = xs.get(itemIdx);
                double x2 = items.get(storedIdx);
                if (x1 < x2) {
                    nextItem = x1;
                    nextWeight = segWeights.get(itemIdx);
                    itemIdx++;
                } else {
                    nextItem = x2;
                    nextWeight = weights.get(storedIdx);
                    storedIdx++;
                }
            }

            if (newItems.isEmpty()) {
                newItems.add(nextItem);
                newWeights.add(nextWeight);
            } else {
                int lastAddedIdx = newItems.size()-1;
                double prevAddedItem = newItems.get(lastAddedIdx);
                if (prevAddedItem == nextItem) {
                    newWeights.set(
                            lastAddedIdx,
                            newWeights.get(lastAddedIdx)+nextWeight
                    );
                } else {
                    newItems.add(nextItem);
                    newWeights.add(nextWeight);
                }
            }
        }

        items = newItems;
        weights = newWeights;
    }

    public CDFSketch calcDelta(SortedQuantileAccumulator other) {
        int n = items.size();
        double[] newCDF = new double[n];
        double otherCDF = 0.0;
        double curCDF = 0.0;

        int otherIdx = 0;
        for (int i = 0; i < n; i++) {
            double curX = items.get(i);
            curCDF += weights.get(i);

            while (otherIdx < other.items.size()) {
                double otherItem = other.items.get(otherIdx);
                if (otherItem <= curX) {
                    otherCDF += other.weights.get(otherIdx);
                    otherIdx++;
                } else {
                    break;
                }
            }
            newCDF[i] = curCDF - otherCDF;
        }

        return new CDFSketch(items, new DoubleArrayList(newCDF));
    }

    public int searchItemIdx(double x) {
        return items.binarySearch(x);
    }

    public CDFSketch getCDF() {
        return CDFSketch.fromWeights(
                items, weights
        );
    }

    @Override
    public void reset() {
        items.clear();
        weights.clear();
    }

    public int compress(int size) {
        return 0;
    }
}
