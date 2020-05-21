package summary.custom;

import org.apache.commons.math3.util.FastMath;
import org.apache.datasketches.frequencies.LongsSketch;
import org.apache.datasketches.kll.KllFloatsSketch;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.Sketch;
import summary.gen.SketchGen;

public class YahooKLLGen implements SketchGen<Double, DoubleList> {
    @Override
    public FastList<Sketch<Double>> generate(DoubleList xs, int size, double bias) {
        FastList<Sketch<Double>> out = new FastList<>(1);
        KllFloatsSketch rawSketch = new KllFloatsSketch(size/3);

        int n = xs.size();
        for (int i = 0; i < n; i++) {
            rawSketch.update((float)xs.get(i));
        }
        out.add(new YahooKLLSketch(rawSketch, size));
        return out;
    }
}
