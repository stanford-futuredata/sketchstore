package summary;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class CounterLongSketch implements Sketch<Long> {
    public long[] values;
    public double[] weights;
    public CounterLongSketch(long[] values, double[] weights) {
        this.values = values;
        this.weights = weights;
    }

    public static CounterLongSketch fromMap(LongDoubleHashMap map) {
        int n = map.size();
        LongArrayList values = new LongArrayList(n);
        DoubleArrayList weights = new DoubleArrayList(n);
        map.forEachKeyValue((long k, double v) -> {
            values.add(k);
            weights.add(v);
        });
        return new CounterLongSketch(
                values.toArray(),
                weights.toArray()
        );
    }

    @Override
    public String name() {
        return "counter_long";
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public String toString() {
        int n = values.length;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            out.append(String.valueOf(values[i]));
            out.append(":");
            out.append(String.valueOf(weights[i]));
            out.append(" ");
        }
        return out.toString();
    }

    @Override
    public Sketch<Long> merge(Sketch<Long> otherArg) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public double estimate(Long xQuery) {
        long xQueryVal = xQuery;
        int n = values.length;
        for (int i = 0; i < n; i++) {
            double x = values[i];
            if (x == xQueryVal) {
                return weights[i];
            }
        }
        return 0;
    }
}
