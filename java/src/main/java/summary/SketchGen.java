package summary;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.io.Serializable;

public interface SketchGen<T extends Serializable, TL> {
    public FastList<BoardSketch<T>> generate(
            TL xs, int size, double bias
    );
}
