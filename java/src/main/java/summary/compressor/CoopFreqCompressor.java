package summary.compressor;

import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

public class CoopFreqCompressor implements ItemDictCompressor{
    LongDoubleHashMap deltas;
    int interval_len;
    int cur_idx;

    public CoopFreqCompressor(int interval_len) {
        this.interval_len = interval_len;
        cur_idx = 0;
        deltas = new LongDoubleHashMap();
    }

    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        return null;
    }
}
