package summary.gen;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import summary.CounterDoubleSketch;
import summary.CounterLongSketch;
import summary.Sketch;
import summary.accumulator.MapFreqAccumulator;
import summary.compressor.freq.ItemDictCompressor;
import summary.compressor.quantile.SeqCounterCompressor;

import java.util.function.Supplier;

public class DyadicSeqCounterCompressorGen implements SketchGen<Double, DoubleList> {
    public int maxHeight;
    public FastList<SeqCounterCompressor> compressorStack;
    public FastList<DoubleArrayList> segmentStack;
    public long[] countdowns;
    public double base = 2.0;

    public DyadicSeqCounterCompressorGen(Supplier<SeqCounterCompressor> cGen, int maxHeight) {
        this.maxHeight = maxHeight;
        compressorStack = new FastList<>(maxHeight);
        segmentStack = new FastList<>(maxHeight);
        countdowns = new long[maxHeight];
        for (int i=0; i < maxHeight; i++) {
            compressorStack.add(cGen.get());
            segmentStack.add(new DoubleArrayList());
            countdowns[i] = (long) FastMath.pow(base, i);
        }
    }

    @Override
    public FastList<Sketch<Double>> generate(DoubleList xs, int size, double bias) {
        double scaledSize = size*1.0/maxHeight;
        FastList<Sketch<Double>> sketches = new FastList<>(maxHeight);
        for (int levelIdx = 0; levelIdx < maxHeight; levelIdx++) {
            DoubleArrayList curSegment = segmentStack.get(levelIdx);
            curSegment.addAll(xs);
            countdowns[levelIdx]--;
            if (countdowns[levelIdx] == 0) {
                int currentSize = (int)Math.round((scaledSize * FastMath.pow(base, levelIdx)));
                curSegment.sortThis();
                CounterDoubleSketch compressedMap = compressorStack.get(levelIdx).compress(
                        curSegment, currentSize
                );
                sketches.add(compressedMap);
                countdowns[levelIdx] = (long) FastMath.pow(base, levelIdx);
                curSegment.clear();
            }
        }
        return sketches;
    }
}
