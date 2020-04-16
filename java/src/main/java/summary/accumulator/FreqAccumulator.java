package summary.accumulator;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.DictSketch;

import java.util.List;

public interface FreqAccumulator {
    void reset();

    void add(LongList xs);

    void add(LongDoubleHashMap curMap);

    FastList<Double> estimate(List<Long> xToTrack);
}
