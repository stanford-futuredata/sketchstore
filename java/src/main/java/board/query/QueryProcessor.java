package board.query;

import board.StoryBoard;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.List;

public interface QueryProcessor<T> {
    DoubleList query(StoryBoard<T> board, List<T> xToTrack);
    double total();
    int span();
}
