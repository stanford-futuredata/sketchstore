package board.planner;

import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SizePlannerTest {
    @Test
    public void testSimple() {
        LongArrayList segmentSizes = new LongArrayList();
        FastList<LongList> segmentDimensions = new FastList<>();
        segmentSizes.add(1);
        segmentSizes.add(10);
        segmentSizes.add(100);
        segmentDimensions.add(LongLists.mutable.of(0,0));
        segmentDimensions.add(LongLists.mutable.of(0,1));
        segmentDimensions.add(LongLists.mutable.of(1,0));
        segmentDimensions.add(LongLists.mutable.of(1,1));

        double workloadProb = 1;
        SizePlanner planner = new SizePlanner(
                segmentSizes,
                segmentDimensions,
                workloadProb
        );
        double[] sizes = planner.calcScaling();
        assertEquals(sizes[0], sizes[2], 1e-7);

        workloadProb = .5;
        planner = new SizePlanner(
                segmentSizes,
                segmentDimensions,
                workloadProb
        );
        sizes = planner.calcScaling();
//        System.out.println(Arrays.toString(sizes));
        assertTrue(sizes[0] * 1.5 < sizes[2]);
    }

}