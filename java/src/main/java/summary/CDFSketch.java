package summary;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

public class CDFSketch implements Sketch<Double> {
    public DoubleArrayList values;
    public DoubleArrayList weights;
    public DoubleArrayList cumTotal;
    public CDFSketch(DoubleList xs, DoubleList weights) {
        this.values = DoubleArrayList.newList(xs);
        this.weights = DoubleArrayList.newList(weights);
        double total = weights.sum();
        int n = weights.size();
        double runningTotal = 0;
        cumTotal = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            runningTotal += weights.get(i);
            cumTotal.set(i, runningTotal);
        }
    }

    @Override
    public String name() {
        return "cdf";
    }

    @Override
    public Sketch<Double> merge(Sketch<Double> otherArg) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public double estimate(Double x) {
        int searchResult = values.binarySearch(x);
        if (searchResult > 0) {
            return cumTotal.get(searchResult);
        } else {
            searchResult = -(searchResult+1);
            if (searchResult == 0) {
                return 0.0;
            } else {
                return cumTotal.get(searchResult-1);
            }
        }
    }
}
