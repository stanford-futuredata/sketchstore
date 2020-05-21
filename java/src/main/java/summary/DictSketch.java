package summary;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class DictSketch implements Sketch<Long> {
    public LongDoubleHashMap vals;
    public DictSketch(LongDoubleHashMap vals) {
        this.vals = vals;
    }

    @Override
    public String name() {
        return "dict";
    }

    @Override
    public int size() {
        return vals.size();
    }

    @Override
    public Sketch<Long> merge(Sketch<Long> otherArg) {
        DictSketch other = (DictSketch)otherArg;
        vals.putAll(other.vals);
        return this;
    }

    @Override
    public double estimate(Long x) {
        return vals.getIfAbsent(x, 0.0);
    }
}
