package board.planner.size;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PropSizeOptimizerTest {
    @Test
    public void testMin() {
        PropSizeOptimizer<LongList> opt = new PropSizeOptimizer<LongList>();
        opt.compute(
                LongLists.mutable.of(100, 1, 1),
                new FastList<>(),
                .2
        );
        int[] sizes = opt.getSizes(10);
        assertEquals(8, sizes[0]);
        assertEquals(1, sizes[1]);
    }

}