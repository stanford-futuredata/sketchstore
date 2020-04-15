package summary;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.compressor.ItemDictCompressor;

public class ItemDictCompressorGen implements FSketchGen {
    public ItemDictCompressor compressor;

    public ItemDictCompressorGen(ItemDictCompressor c) {
        compressor = c;
    }

    public static LongDoubleHashMap aggregate(LongList xs) {
        LongDoubleHashMap xCounts = new LongDoubleHashMap();
        for (int i = 0; i < xs.size(); i++) {
            xCounts.addToValue(xs.get(i), 1.0);
        }
        return xCounts;
    }

    @Override
    public FastList<BoardSketch<Long>> generate(LongList xs, int size, double bias) {

        return null;
    }
}
