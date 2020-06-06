package summary.compressor.freq;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;

public class ExactFreqCompressor implements ItemCounterCompressor {
    @Override
    public CounterLongSketch compress(LongDoubleHashMap xs, int size) {
        return CounterLongSketch.fromMap(xs);
    }
}
