package summary.accumulator;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import summary.Sketch;
import summary.gen.SketchGen;

import java.util.List;
import java.util.function.Supplier;

public class MergingAccumulator<T, TL extends PrimitiveIterable> implements Accumulator<T, TL>{
    SketchGen<T, TL> sketchGenerator;
    Sketch<T> merged;
    TL emptyList;

    public MergingAccumulator(SketchGen<T, TL> sketchGenerator, TL emptyList) {
        this.sketchGenerator = sketchGenerator;
        this.emptyList = emptyList;
    }

    @Override
    public void reset() {
        merged = null;
    }

    @Override
    public int compress(int size) {
        return 0;
    }

    @Override
    public void addRaw(TL xs) {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void addSketch(Sketch<T> newSketch) {
        if (merged == null) {
            merged = sketchGenerator.generate(
                    emptyList,
                    newSketch.size(),
                    0
            ).get(0);
        }
        merged.merge(newSketch);
    }

    @Override
    public DoubleList estimate(List<T> xToTrack) {
        int n = xToTrack.size();
        DoubleArrayList out = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            out.add(merged.estimate(xToTrack.get(i)));
        }
        return out;
    }
}
