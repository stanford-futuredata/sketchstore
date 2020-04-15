package summary;

import java.io.Serializable;

public interface BoardSketch<T extends Serializable> extends Serializable {
    String name();
    BoardSketch<T> merge(BoardSketch<T> other);
    double estimate(T x);
}

