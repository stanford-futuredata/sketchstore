package summary;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.impl.list.mutable.FastList;

public interface SketchGen<T, TL extends PrimitiveIterable> {
    public FastList<Sketch<T>> generate(
            TL xs, int size, double bias
    );
}
