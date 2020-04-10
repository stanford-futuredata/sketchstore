package summary.compressor;

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;


public interface ItemDictCompressor {
    LongDoubleHashMap compress(LongDoubleHashMap xs, int size);
}
