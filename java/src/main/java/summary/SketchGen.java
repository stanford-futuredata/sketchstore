package summary;

import org.eclipse.collections.impl.list.mutable.FastList;

public interface SketchGen<T, TL> {
    public FastList<Sketch<T>> generate(
            TL xs, int size, double bias
    );
}
