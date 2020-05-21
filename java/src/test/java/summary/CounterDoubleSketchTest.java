package summary;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.junit.Test;

import static org.junit.Assert.*;

public class CounterDoubleSketchTest {
    @Test
    public void testSimple() {
        double[] xs = {1.0, 2.0, 3.0};
        double[] weights = {5.0, 5.0, 2.0};
        CounterDoubleSketch sketch = new CounterDoubleSketch(xs, weights);
        assertEquals(10.0, sketch.estimate(2.5), 1e-10);
    }

}