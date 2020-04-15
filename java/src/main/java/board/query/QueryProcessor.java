package board.query;

import board.SketchBoard;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.BoardSketch;

import java.io.Serializable;

public interface QueryProcessor<T extends Serializable> {
    FastList<Double> query(SketchBoard<T> board, FastList<T> xToTrack);
}
