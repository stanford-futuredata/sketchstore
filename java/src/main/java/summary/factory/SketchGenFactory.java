package summary.factory;

import org.eclipse.collections.api.PrimitiveIterable;
import summary.accumulator.Accumulator;
import summary.gen.SketchGen;

import java.util.List;

public interface SketchGenFactory<T, TL extends PrimitiveIterable> {
    SketchGen<T, TL> getSketchGen(
            String sketch,
            List<T> xToTrack
    );
    Accumulator<T, TL> getAccumulator(
            String sketch
    );
}
