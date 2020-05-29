package summary.gen;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.DictSketch;
import summary.Sketch;
import summary.accumulator.MapFreqAccumulator;
import summary.compressor.freq.ItemDictCompressor;

public class ItemDictCompressorGen implements SketchGen<Long, LongList> {
    public ItemDictCompressor compressor;

    public ItemDictCompressorGen(ItemDictCompressor c) {
        compressor = c;
    }

    @Override
    public FastList<Sketch<Long>> generate(LongList xs, int size, double bias) {
        MapFreqAccumulator acc = new MapFreqAccumulator();
        acc.addRaw(xs);
        LongDoubleHashMap biasedCounts;
        if (bias >= 1) {
            biasedCounts = new LongDoubleHashMap(acc.values.size());
            acc.values.forEachKeyValue((long k, double v) -> {
                if (v > bias) {
                    biasedCounts.put(k, v-bias);
                }
            });
        } else {
            biasedCounts = acc.values;
        }

        LongDoubleHashMap biasedResults = compressor.compress(biasedCounts, size);
        LongDoubleHashMap adjBiasedResults = new LongDoubleHashMap(biasedResults.size());
        biasedResults.forEachKeyValue((long k, double v) -> {
            adjBiasedResults.put(k, v+bias);
        });
        Sketch<Long> sketch = new DictSketch(adjBiasedResults);
        FastList<Sketch<Long>> sketches = new FastList<>(1);
        sketches.add(sketch);
        return sketches;
    }
}
