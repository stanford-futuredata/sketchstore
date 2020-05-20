package summary.accumulator;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;

import java.util.List;

public interface Accumulator<T, TL extends PrimitiveIterable> {
    void reset();
    int compress(int size);

    void addRaw(TL xs);
    void addSketch(Object newSketch);

    DoubleList estimate(List<T> xToTrack);
}
