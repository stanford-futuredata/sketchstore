package summary.custom;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.Sketch;
import summary.gen.SketchGen;

public class CMSSketchGen implements SketchGen<Long, LongList> {
    @Override
    public FastList<Sketch<Long>> generate(LongList xs, int size, double bias) {
        FastList<Sketch<Long>> out = new FastList<>(1);
        PatchedCountMinSketch rawSketch = new PatchedCountMinSketch(5, size, 0);

        int n = xs.size();
        for (int i = 0; i < n; i++) {
            rawSketch.add(xs.get(i), 1);
        }
        out.add(new CMSSketch(rawSketch, size));
        return out;
    }
}
