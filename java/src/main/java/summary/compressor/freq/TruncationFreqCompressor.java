package summary.compressor.freq;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;
import summary.accumulator.ArgSort;

public class TruncationFreqCompressor implements ItemCounterCompressor {
    @Override
    public CounterLongSketch compress(LongDoubleHashMap xs, int size) {
        if (size >= xs.size()) {
            return CounterLongSketch.fromMap(xs);
        }
        int nXs = xs.size();
        LongArrayList xList = new LongArrayList(nXs);
        DoubleArrayList negWeightList = new DoubleArrayList(nXs);
        xs.forEachKeyValue((long k, double v) -> {
            xList.add(k);
            negWeightList.add(-v);
        });
        int[] orderedIdxs = ArgSort.argSort(negWeightList);

        int nToStore = size;
        if (nToStore > nXs) {
            nToStore = nXs;
        }
        long[] xToStore = new long[nToStore];
        double[] wToStore = new double[nToStore];
        for (int i = 0; i < nToStore; i++){
            int curOrderedIdx = orderedIdxs[i];
            xToStore[i] = xList.get(curOrderedIdx);
            wToStore[i] = -negWeightList.get(curOrderedIdx);
        }
        return new CounterLongSketch(xToStore, wToStore);
    }
}
