package summary.custom;

import org.apache.commons.math3.util.FastMath;
import org.apache.datasketches.frequencies.LongsSketch;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.Sketch;
import summary.gen.SketchGen;

public class YahooMGGen implements SketchGen<Long, LongList> {
    @Override
    public FastList<Sketch<Long>> generate(LongList xs, int size, double bias) {
        FastList<Sketch<Long>> out = new FastList<>(1);
        int adjustedSize = 1 << (int)(FastMath.ceil(FastMath.log(2.0, size)));
        LongsSketch rawSketch = new LongsSketch(adjustedSize);

        int n = xs.size();
        for (int i = 0; i < n; i++) {
            rawSketch.update(xs.get(i));
        }
        out.add(new YahooMGSketch(rawSketch, size));
        return out;
    }
}
