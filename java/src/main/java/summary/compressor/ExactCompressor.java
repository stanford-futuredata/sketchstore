package summary.compressor;

import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class ExactCompressor implements ItemDictCompressor{
    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        return new LongDoubleHashMap(xs);
    }
}
