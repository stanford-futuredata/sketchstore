package summary.gen;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.CDFSketch;
import summary.DictSketch;
import summary.Sketch;
import summary.accumulator.ExactFreqAccumulator;
import summary.compressor.freq.ItemDictCompressor;
import summary.compressor.quantile.SeqCDFCompressor;

public class SeqCDFCompressorGen implements SketchGen<Double, DoubleList> {
    public SeqCDFCompressor compressor;

    public SeqCDFCompressorGen(SeqCDFCompressor c) {
        compressor = c;
    }

    @Override
    public FastList<Sketch<Double>> generate(DoubleList xs, int size, double bias) {
        MutableDoubleList xSorted = xs.toSortedList();
        Sketch<Double> sketch = compressor.compress(xSorted, size);
        FastList<Sketch<Double>> sketches = new FastList<>(1);
        sketches.add(sketch);
        return sketches;
    }
}
