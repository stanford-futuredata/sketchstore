package summary.compressor.quantile;

import org.eclipse.collections.api.list.primitive.DoubleList;
import summary.CounterDoubleSketch;

public interface SeqCounterCompressor {
    /**
     * @param xs sorted list of input values
     * @param size number of items to store
     * @return sorted item, weight pairs
     */
    CounterDoubleSketch compress(DoubleList xs, int size);
}
