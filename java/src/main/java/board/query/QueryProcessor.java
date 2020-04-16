package board.query;

import board.SketchBoard;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;
import summary.BoardSketch;

import java.io.Serializable;
import java.util.List;

public interface QueryProcessor<T> {
    FastList<Double> query(SketchBoard<T> board, List<T> xToTrack);
}
