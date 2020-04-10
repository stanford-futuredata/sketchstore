package summary;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class DictSketch implements BoardSketch {
    public LongDoubleHashMap vals;
    public DictSketch(LongDoubleHashMap vals) {
        this.vals = vals;
    }

    @Override
    public String name() {
        return "dict";
    }

    @Override
    public double estimate(int x) {
        return vals.getIfAbsent(x, 0.0);
    }

    @Override
    public double estimate(double x) {
        return 0;
    }
}
