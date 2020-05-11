package board.workload;

import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;

import static org.junit.Assert.*;

public class LinearWorkloadTest {
    @Test
    public void testSimple() {
        LinearWorkload workload = new LinearWorkload(0);
        FastList<IntList> intervals = workload.generate(128, 8);
        assertEquals(1,intervals.get(0).get(1) - intervals.get(0).get(0));
    }
}