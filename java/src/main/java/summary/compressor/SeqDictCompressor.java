package summary.compressor;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleDoubleHashMap;

public interface SeqDictCompressor {
    DoubleDoubleHashMap compress(DoubleArrayList xs, int size);
}
