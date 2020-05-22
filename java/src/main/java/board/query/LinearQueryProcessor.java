package board.query;

public interface LinearQueryProcessor<T> extends QueryProcessor<T> {
    void setRange(int startIdx, int endIdx);
}
