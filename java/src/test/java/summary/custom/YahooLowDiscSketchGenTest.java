package summary.custom;

import io.IOUtil;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import summary.Sketch;

import java.io.IOException;

import static org.junit.Assert.*;

public class YahooLowDiscSketchGenTest {
    @Test
    public void testSimple() throws IOException, ClassNotFoundException {
        YahooLowDiscSketchGen gen = new YahooLowDiscSketchGen();
        int n = 100;
        DoubleArrayList xs = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        Sketch<Double> sketch = gen.generate(
                xs,
                10,
                0
        ).get(0);
        YahooLowDiscSketch sketchObj = (YahooLowDiscSketch) sketch;
        assertTrue(sketchObj.sketch.getRetainedItems() > 5);
        assertTrue(sketchObj.sketch.getRetainedItems() < 20);
        sketch.merge(sketch);
        assertEquals(200.0, sketch.estimate(100.0), 1e-10);

        YahooLowDiscSketch storedSketch = (YahooLowDiscSketch) IOUtil.testSerDe(sketchObj);
        assertEquals(storedSketch.estimate(100.0), sketch.estimate(100.0), 1e-10);
    }

}