package board.workload;

import org.apache.commons.math3.util.FastMath;
import org.eclipse.collections.api.factory.list.FixedSizeListFactory;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class LinearWorkload {
    Random random;

    public LinearWorkload(
            int seed
    ) {
        random = new Random(seed);
    }

    public FastList<IntList> generate(
            int granularity,
            int numQueries
    ) {
        FastList<IntList> intervals = new FastList<>(numQueries*10);

        int maxPower = (int)Math.floor(FastMath.log(2, granularity-100));
        FastList<Integer> queryLengths = new FastList<>(maxPower+1);
        for (int i = 0; i <= maxPower; i++) {
            queryLengths.add((int)FastMath.pow(2, i));
        }

        for (int curQueryLength : queryLengths) {
            for (int i = 0; i < numQueries; i++) {
                int curStartIdx = random.nextInt(granularity-curQueryLength+1);
                IntList newInterval = IntLists.mutable.of(curStartIdx, curStartIdx+curQueryLength);
                intervals.add(newInterval);
            }
        }

        return intervals;
    }
}
