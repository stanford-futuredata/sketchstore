package board.query;

import board.StoryBoard;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.List;

public interface QueryProcessor<T> {
    FastList<Double> query(StoryBoard<T> board, List<T> xToTrack);
}
