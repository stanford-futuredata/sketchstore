package summary.compressor.freq;

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;

import java.util.List;

public class TrackedFreqCompressor implements ItemCounterCompressor {
    public LongArrayList xToTrack;

    public TrackedFreqCompressor(List<Long> xToTrack) {
        this.xToTrack = new LongArrayList(xToTrack.size());
        for (long x : xToTrack) {
            this.xToTrack.add(x);
        }
    }

    @Override
    public CounterLongSketch compress(LongDoubleHashMap xs, int size) {
        int nTrack = xToTrack.size();
        long[] itemsToStore = new long[nTrack];
        double[] weightsToStore = new double[nTrack];
        for (int i = 0; i < nTrack; i++){
            long curX = xToTrack.get(i);
            double curXWeight = xs.getIfAbsent(curX, 0.0);
            itemsToStore[i] = curX;
            weightsToStore[i] = curXWeight;
        }
        return new CounterLongSketch(itemsToStore, weightsToStore);
    }
}
