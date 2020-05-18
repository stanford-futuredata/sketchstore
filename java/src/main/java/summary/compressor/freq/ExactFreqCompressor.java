package summary.compressor.freq;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class ExactFreqCompressor implements ItemDictCompressor{
    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        return new LongDoubleHashMap(xs);
    }
}
