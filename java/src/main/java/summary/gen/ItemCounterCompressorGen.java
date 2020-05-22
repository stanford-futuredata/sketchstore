package summary.gen;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;
import summary.DictSketch;
import summary.Sketch;
import summary.accumulator.MapFreqAccumulator;
import summary.compressor.freq.ItemDictCompressor;

public class ItemCounterCompressorGen implements SketchGen<Long, LongList> {
    public ItemDictCompressor compressor;

    public ItemCounterCompressorGen(ItemDictCompressor c) {
        compressor = c;
    }

    @Override
    public FastList<Sketch<Long>> generate(LongList xs, int size, double bias) {
        MapFreqAccumulator acc = new MapFreqAccumulator();
        acc.addRaw(xs);
        LongDoubleHashMap out = compressor.compress(acc.values, size);
        CounterLongSketch sketch = CounterLongSketch.fromMap(out);
        FastList<Sketch<Long>> sketches = new FastList<>(1);
        sketches.add(sketch);
        return sketches;
    }
}
