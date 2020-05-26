package board.workload;

import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;

import static org.junit.Assert.*;

public class CubeWorkloadTest {
    @Test
    public void testSimple() {
        CubeWorkload workload = new CubeWorkload(0);
        int nDims = 3;
        LongArrayList dimCards = new LongArrayList(nDims);
        dimCards.add(3);
        dimCards.add(2);
        dimCards.add(1);
        FastList<LongList> cubeDimensions = workload.generate(dimCards, .5, 10);
        assertEquals(10, cubeDimensions.size());
        assertEquals(nDims, cubeDimensions.get(0).size());
    }

}