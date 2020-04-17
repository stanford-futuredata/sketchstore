package summary;

import java.io.Serializable;

public interface Sketch<T> extends Serializable {
    String name();
    Sketch<T> merge(Sketch<T> other);
    double estimate(T x);
}

