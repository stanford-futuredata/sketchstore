package summary.compressor.freq;

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;


public interface ItemCounterCompressor {
    CounterLongSketch compress(LongDoubleHashMap xs, int size);
}
