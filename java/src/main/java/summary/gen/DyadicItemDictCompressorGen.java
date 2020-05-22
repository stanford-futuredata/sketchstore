package summary.gen;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterLongSketch;
import summary.DictSketch;
import summary.Sketch;
import summary.accumulator.MapFreqAccumulator;
import summary.compressor.freq.ItemDictCompressor;

import java.util.function.Supplier;

public class DyadicItemDictCompressorGen implements SketchGen<Long, LongList> {
    public int maxHeight;
    public FastList<ItemDictCompressor> compressorStack;
    public FastList<MapFreqAccumulator> segmentStack;
    public long[] countdowns;
    public double base = 2.0;

    public DyadicItemDictCompressorGen(Supplier<ItemDictCompressor> cGen, int maxHeight) {
        this.maxHeight = maxHeight;
        compressorStack = new FastList<>(maxHeight);
        segmentStack = new FastList<>(maxHeight);
        countdowns = new long[maxHeight];
        for (int i=0; i < maxHeight; i++) {
            compressorStack.add(cGen.get());
            segmentStack.add(new MapFreqAccumulator());
            countdowns[i] = (long) FastMath.pow(base, i);
        }
    }

    @Override
    public FastList<Sketch<Long>> generate(LongList xs, int size, double bias) {
        double scaledSize = size*1.0/maxHeight;
        FastList<Sketch<Long>> sketches = new FastList<>(maxHeight);
        for (int levelIdx = 0; levelIdx < maxHeight; levelIdx++) {
            MapFreqAccumulator curSegment = segmentStack.get(levelIdx);
            curSegment.addRaw(xs);
            countdowns[levelIdx]--;
            if (countdowns[levelIdx] == 0) {
                int currentSize = (int)(scaledSize * FastMath.pow(base, levelIdx));
                LongDoubleHashMap compressedMap = compressorStack.get(levelIdx).compress(
                        curSegment.values, currentSize
                );
                sketches.add(CounterLongSketch.fromMap(compressedMap));
                countdowns[levelIdx] = (long) FastMath.pow(base, levelIdx);
                curSegment.reset();
            }
        }
        return sketches;
    }
}
