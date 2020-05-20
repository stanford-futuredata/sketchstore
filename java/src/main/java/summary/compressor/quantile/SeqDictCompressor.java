package summary.compressor.quantile;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.CDFSketch;

public interface SeqDictCompressor {
    /**
     * @param xs sorted list of input values
     * @param size number of items to store
     * @return sorted item, weight pairs
     */
    CDFSketch compress(DoubleArrayList xs, int size);
}
