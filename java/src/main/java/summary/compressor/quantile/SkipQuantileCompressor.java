package summary.compressor.quantile;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleDoubleHashMap;

public class SkipQuantileCompressor implements SeqDictCompressor {
    @Override
    public DoubleDoubleHashMap compress(DoubleArrayList xs, int size) {
        xs.sortThis();
        int n = xs.size();
        int skip = (int)Math.ceil(n*1.0/size);
        DoubleDoubleHashMap result
        return null;
    }
}
