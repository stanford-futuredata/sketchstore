package board.query;

import org.eclipse.collections.api.list.primitive.LongList;

public interface CubeQueryProcessor<T> extends QueryProcessor<T> {
    void setDimensions(LongList dims);
}
