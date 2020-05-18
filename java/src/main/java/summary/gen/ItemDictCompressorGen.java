package summary.gen;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.DictSketch;
import summary.Sketch;
import summary.accumulator.ExactFreqAccumulator;
import summary.compressor.freq.ItemDictCompressor;

public class ItemDictCompressorGen implements SketchGen<Long, LongList> {
    public ItemDictCompressor compressor;

    public ItemDictCompressorGen(ItemDictCompressor c) {
        compressor = c;
    }

    @Override
    public FastList<Sketch<Long>> generate(LongList xs, int size, double bias) {
        ExactFreqAccumulator acc = new ExactFreqAccumulator();
        acc.add(xs);
        Sketch<Long> sketch = new DictSketch(
                compressor.compress(acc.values, size)
        );
        FastList<Sketch<Long>> sketches = new FastList<>(1);
        sketches.add(sketch);
        return sketches;
    }
}
