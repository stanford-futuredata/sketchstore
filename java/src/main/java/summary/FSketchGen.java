package summary;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.io.Serializable;

public interface FSketchGen {
    public FastList<BoardSketch<Long>> generate(
            LongList xs, int size, double bias
    );
}
