package summary.compressor;

import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class TruncationCompressor implements ItemDictCompressor{
    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        if (size >= xs.size()) {
            return xs;
        }
        double[] xWeights = xs.values().toSortedArray();
        double maxWeight = xWeights[xWeights.length-size];

        LongDoubleHashMap newMap = new LongDoubleHashMap(size);
        for (LongDoublePair xv : xs.keyValuesView()) {
            double curWeight = xv.getTwo();
            if (curWeight >= maxWeight && newMap.size() < size) {
                newMap.put(xv.getOne(), curWeight);
            }
        }
        return newMap;
    }
}
