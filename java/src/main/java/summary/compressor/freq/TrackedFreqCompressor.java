package summary.compressor.freq;

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

import java.util.List;

public class TrackedFreqCompressor implements ItemDictCompressor{
    public LongArrayList xToTrack;

    public TrackedFreqCompressor(List<Long> xToTrack) {
        this.xToTrack = new LongArrayList(xToTrack.size());
        for (long x : xToTrack) {
            this.xToTrack.add(x);
        }
    }

    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        LongDoubleHashMap newMap = new LongDoubleHashMap(xToTrack.size());
        int n = xToTrack.size();
        for (int i = 0; i < n; i++){
            long curX = xToTrack.get(i);
            newMap.put(curX, xs.get(curX));
        }
        return newMap;
    }
}
