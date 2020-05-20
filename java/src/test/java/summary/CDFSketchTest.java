package summary;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

public class CDFSketchTest {
    @Test
    public void testSimple() {
        DoubleList xs = DoubleLists.mutable.of(1.0, 2.0, 3.0);
        DoubleList weights = DoubleLists.mutable.of(5.0, 5.0, 2.0);
        CDFSketch sketch = CDFSketch.fromWeights(xs, weights);
        assertEquals(10.0, sketch.estimate(2.5), 1e-10);
    }

}