package board.planner.size;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;

public interface SizeOptimizer<TL extends PrimitiveIterable> {
    int[] getSizes(int totalSize);
    void compute(
            LongList segmentSizes,
            FastList<LongList> segmentDimensions,
            double workloadProb
    );
}
