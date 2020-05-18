package summary.compressor.freq;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class TruncationFreqCompressor implements ItemDictCompressor{
    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        if (size >= xs.size()) {
            return xs;
        }
        MutableList<LongDoublePair> values = xs.keyValuesView().toSortedListBy((LongDoublePair xp) -> -xp.getTwo());
        LongDoubleHashMap newMap = new LongDoubleHashMap(size);
        for (LongDoublePair xv : values) {
            if (newMap.size() < size) {
                newMap.put(xv.getOne(), xv.getTwo());
            }
        }
        return newMap;
    }
}
