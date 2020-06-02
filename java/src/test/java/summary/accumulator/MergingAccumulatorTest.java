package summary.accumulator;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.junit.Test;
import runner.factory.FreqSketchGenFactory;
import summary.custom.CMSSketchGen;

import static org.junit.Assert.*;

public class MergingAccumulatorTest {
    @Test
    public void testNull() {
        FreqSketchGenFactory factory = new FreqSketchGenFactory();
        MergingAccumulator<Long, LongList> acc = (MergingAccumulator<Long, LongList>) factory.getAccumulator("cms_min");
        DoubleList results = acc.estimate(Lists.mutable.of(1L, 2L, 3L));
        assertEquals(0, results.get(0), 1e-10);
    }

}