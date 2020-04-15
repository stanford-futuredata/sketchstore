package summary;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.accumulator.ExactFreqAccumulator;
import summary.compressor.ItemDictCompressor;

public class ItemDictCompressorGen implements SketchGen<Long, LongList> {
    public ItemDictCompressor compressor;

    public ItemDictCompressorGen(ItemDictCompressor c) {
        compressor = c;
    }

    @Override
    public FastList<BoardSketch<Long>> generate(LongList xs, int size, double bias) {
        ExactFreqAccumulator acc = new ExactFreqAccumulator();
        acc.add(xs);
        BoardSketch<Long> sketch = new DictSketch(
                compressor.compress(acc.values, size)
        );
        FastList<BoardSketch<Long>> sketches = new FastList<>(1);
        sketches.add(sketch);
        return sketches;
    }
}
