package summary.gen;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.MutableDoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.Sketch;
import summary.compressor.quantile.SeqCounterCompressor;

public class SeqCounterCompressorGen implements SketchGen<Double, DoubleList> {
    public SeqCounterCompressor compressor;

    public SeqCounterCompressorGen(SeqCounterCompressor c) {
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
