package board.planner.bias;

import org.junit.Test;

import static org.junit.Assert.*;

public class RMSErrorFunctionTest {
    @Test
    public void testCCDF() {
        long[] occCounts = {1, 2, 3, 5};
        long[] occCountFreqs = {50, 30, 10, 10};
        RMSErrorFunction.SegmentCCDF ccdf = new RMSErrorFunction.SegmentCCDF(
                occCounts,
                occCountFreqs
        );
        assertEquals(50+30*2+10*3+10*5,ccdf.total(0).getOne(),1e-10);
        assertEquals(30+10*2+10*4,ccdf.total(1).getOne(),1e-10);
        assertEquals(-50,ccdf.total(1).getTwo(),1e-10);
    }
}