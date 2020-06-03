package summary.custom;

import org.apache.commons.math3.util.FastMath;
import org.apache.datasketches.kll.KllFloatsSketch;
import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.DoublesSketchBuilder;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.Sketch;
import summary.gen.SketchGen;

public class YahooLowDiscSketchGen implements SketchGen<Double, DoubleList> {
    @Override
    public FastList<Sketch<Double>> generate(DoubleList xs, int size, double bias) {
        FastList<Sketch<Double>> out = new FastList<>(1);
        int adjustedSize = 1 << (int)(FastMath.ceil(FastMath.log(2.0, size/4.0)));
        adjustedSize = Math.max(2, adjustedSize);
        DoublesSketchBuilder builder = DoublesSketch.builder();
        builder.setK(adjustedSize);
        UpdateDoublesSketch rawSketch = builder.build();

        int n = xs.size();
        for (int i = 0; i < n; i++) {
            rawSketch.update(xs.get(i));
        }
        out.add(new YahooLowDiscSketch(rawSketch, size));
        return out;
    }
}
