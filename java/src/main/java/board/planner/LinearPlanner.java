package board.planner;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import tech.tablesaw.api.Table;

import java.util.List;

public interface LinearPlanner<TL extends PrimitiveIterable> extends Planner<TL> {
    void plan(
            Table t, String metricCol, int numSegments, int size
    );
}
