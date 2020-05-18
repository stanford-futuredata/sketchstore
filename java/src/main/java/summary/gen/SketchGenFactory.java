package summary.gen;

import org.eclipse.collections.api.PrimitiveIterable;

import java.util.List;

public interface SketchGenFactory<T, TL extends PrimitiveIterable> {
    SketchGen<T, TL> getSketchGen(
            String sketch,
            List<T> xToTrack
    );
}
