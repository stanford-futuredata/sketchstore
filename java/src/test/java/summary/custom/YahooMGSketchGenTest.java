package summary.custom;

import board.StoryBoard;
import io.IOUtil;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.junit.Test;
import summary.accumulator.MergingAccumulator;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class YahooMGSketchGenTest {
    @Test
    public void testSimple() throws IOException, ClassNotFoundException {
        YahooMGGen gen = new YahooMGGen();
        LongList xs = LongLists.mutable.of(1,2,3,1,1,1);
        YahooMGSketch sketch = (YahooMGSketch) gen.generate(
                xs, 64, 0
        ).get(0);
        assertEquals(4.0, sketch.estimate(1l), 1e-10);

        YahooMGSketch storedSketch = (YahooMGSketch) IOUtil.testSerDe(sketch);
        assertEquals(1.0, storedSketch.estimate(2l), 1e-10);
    }

    @Test
    public void testMerge() {
        YahooMGGen gen = new YahooMGGen();
        LongList xs = LongLists.mutable.of(1,2,3,1,1,1);
        YahooMGSketch sketch1 = (YahooMGSketch) gen.generate(
                xs, 64, 0
        ).get(0);
        YahooMGSketch sketch2 = (YahooMGSketch) gen.generate(
                xs, 64, 0
        ).get(0);

        MergingAccumulator<Long, LongList> acc = new MergingAccumulator<>(
                gen,
                LongLists.immutable.empty()
        );
        acc.addSketch(sketch1);
        acc.addSketch(sketch2);
        assertEquals(8.0, acc.estimate(Lists.fixedSize.of(1l)).get(0), 1e-10);
    }

}