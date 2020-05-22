package summary.custom;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.junit.Test;
import summary.Sketch;
import summary.accumulator.MergingAccumulator;

import static org.junit.Assert.*;

public class CMSSketchGenTest {
    @Test
    public void testMerge() {
        CMSSketchGen gen = new CMSSketchGen();
        LongList xs = LongLists.mutable.of(1,2,3,1,1,1);
        Sketch<Long> sketch1 = gen.generate(
                xs, 64, 0
        ).get(0);
        Sketch<Long> sketch2 = gen.generate(
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