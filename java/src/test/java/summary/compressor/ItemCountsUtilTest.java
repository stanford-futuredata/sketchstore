package summary.compressor;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

public class ItemCountsUtilTest {
    @Test
    public void testFindT() {
        double[] weightVals = {10.0, 5.0, 3.0, 2.0};
        DoubleList weights = new DoubleArrayList(weightVals);
        double tVal = ItemCountsUtil.find_t(weights, 3);
        assertEquals(5.0, tVal, 1e-10);
    }

}