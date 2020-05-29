package board.planner.bias;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class RMSErrorFunctionTest {
    @Test
    public void testCCDF() {
        long[] occCounts = {1, 2, 3, 5};
        long[] occCountFreqs = {50, 30, 10, 10};
        SegmentCCDF ccdf = new SegmentCCDF(
                occCounts,
                occCountFreqs
        );
        assertEquals(50+30*2+10*3+10*5,ccdf.total(0).getOne(),1e-10);
        assertEquals(30+10*2+10*4,ccdf.total(1).getOne(),1e-10);
        assertEquals(-50,ccdf.total(1).getTwo(),1e-10);
    }

    @Test
    public void testConstruct() {
        long[] xs = {1, 2, 3, 1, 1, 1, 4, 5, 5, 4, 4, 4};
        LongList xList = new LongArrayList(xs);
        SegmentCCDF ccdf = SegmentCCDF.fromItems(xList);
        assertEquals(1, ccdf.occCounts[0]);
        assertEquals(2, ccdf.occCountFrequency[0]);
    }
}