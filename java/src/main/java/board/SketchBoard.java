package board;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.BoardSketch;

import java.io.Serializable;

public class SketchBoard implements Serializable {
    FastList<LongList> dimensionCols;
    FastList<BoardSketch> sketches;

    public SketchBoard() {
    }
}
