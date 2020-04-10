package summary;

import java.io.Serializable;

public interface BoardSketch extends Serializable {
    String name();
    double estimate(int x);
    double estimate(double x);
}
