package board.planner;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import tech.tablesaw.api.Table;

import java.io.Serializable;
import java.util.List;

public interface Planner<TL extends PrimitiveIterable> {
    void plan(
            Table t, String metricCol, List<String> dimCols
    );
    public FastList<TL> getSegments();
    public FastList<LongList> getDimensions();
    public IntList getSizes();
    public DoubleList getBiases();
}
