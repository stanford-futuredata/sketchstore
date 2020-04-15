package summary;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class DictSketch implements BoardSketch<Long>{
    public LongDoubleHashMap vals;
    public DictSketch(LongDoubleHashMap vals) {
        this.vals = vals;
    }

    @Override
    public String name() {
        return "dict";
    }

    @Override
    public BoardSketch merge(BoardSketch otherArg) {
        DictSketch other = (DictSketch)otherArg;
        vals.putAll(other.vals);
        return this;
    }

    @Override
    public double estimate(Long x) {
        return vals.getIfAbsent(x, 0.0);
    }
}
