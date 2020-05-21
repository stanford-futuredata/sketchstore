package summary;

import java.io.Serializable;

public interface Sketch<T> extends Serializable {
    String name();
    int size();
    Sketch<T> merge(Sketch<T> other);
    double estimate(T x);
}

