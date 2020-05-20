package summary;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

public class CDFSketch implements Sketch<Double> {
    public DoubleArrayList values;
    public DoubleArrayList cumTotal;
    public CDFSketch(DoubleArrayList values, DoubleArrayList cumTotal) {
        this.values = values;
        this.cumTotal = cumTotal;
    }
    public static CDFSketch fromWeights(DoubleList xs, DoubleList weights) {
        DoubleArrayList values = new DoubleArrayList(xs.toArray());
        int n = weights.size();
        double runningTotal = 0;
        DoubleArrayList cumTotal = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            runningTotal += weights.get(i);
            cumTotal.add(runningTotal);
        }
        return new CDFSketch(values, cumTotal);
    }

    public double getWeight(int index) {
        double weight = cumTotal.get(index);
        if (index > 0) {
            weight -= cumTotal.get(index - 1);
        }
        return weight;
    }

    @Override
    public String toString() {
        int n = values.size();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            out.append(String.valueOf(values.get(i)));
            out.append(":");
            out.append(String.valueOf(cumTotal.get(i)));
            out.append(" ");
        }
        return out.toString();
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
