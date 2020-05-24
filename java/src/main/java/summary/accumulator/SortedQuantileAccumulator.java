package summary.accumulator;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CounterDoubleSketch;
import summary.Sketch;

import java.util.List;
import java.util.Random;

public class SortedQuantileAccumulator implements Accumulator<Double, DoubleList> {
    public DoubleArrayList items;
    public DoubleArrayList weights;
    public Random rng;
    public SortedQuantileAccumulator() {
        items = new DoubleArrayList();
        weights = new DoubleArrayList();
        this.rng = new Random(0);
    }
    public SortedQuantileAccumulator(DoubleArrayList items, DoubleArrayList weights) {
        this.items = items;
        this.weights = weights;
        this.rng = new Random(0);
    }

    public int size() {
        return items.size();
    }

    @Override
    public String toString() {
        int n = items.size();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            out.append(String.valueOf(items.get(i)));
            out.append(":");
            out.append(String.valueOf(weights.get(i)));
            out.append(" ");
        }
        return out.toString();
    }

    @Override
    public void addRaw(DoubleList xs) {
        add(xs, DoubleArrayList.newWithNValues(xs.size(), 1.0));
    }

    @Override
    public void addSketch(Sketch<Double> curObject) {
        assert(curObject instanceof CounterDoubleSketch);
        CounterDoubleSketch sketch = (CounterDoubleSketch) curObject;
        double[] values = sketch.values;
        double[] weights = sketch.weights;
        add(new DoubleArrayList(values), new DoubleArrayList(weights));
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

    public double[] calcDelta(SortedQuantileAccumulator other) {
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

        return newCDF;
    }

    public int searchItemIdx(double x) {
        return items.binarySearch(x);
    }

    @Override
    public void reset() {
        items.clear();
        weights.clear();
    }

    public int compress(int size) {
        double wTotal = weights.sum();
        double sectionWeight = wTotal / size;
        double randCutoff = rng.nextDouble()*sectionWeight;
        int n = items.size();

        DoubleArrayList newItems = new DoubleArrayList(size);
        DoubleArrayList newWeights = new DoubleArrayList(size);

        double totalWeight = 0;
        for (int i = 0; i < n; i++) {
            double curItem = items.get(i);
            double curWeight = weights.get(i);
            if (curWeight >= sectionWeight) {
                newItems.add(curItem);
                newWeights.add(curWeight);
            } else {
                totalWeight += curWeight;
                if (totalWeight >= randCutoff) {
                    newItems.add(curItem);
                    newWeights.add(sectionWeight);
                    totalWeight -= sectionWeight;
                }
            }
        }
        items = newItems;
        weights = newWeights;
        return items.size();
    }
}
