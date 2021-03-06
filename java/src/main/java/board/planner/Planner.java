package board.planner;

import org.eclipse.collections.api.PrimitiveIterable;
import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.list.mutable.FastList;
import tech.tablesaw.api.Table;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Planner<TL extends PrimitiveIterable> {
    FastList<TL> getSegments();
    FastList<LongList> getDimensions();
    void setParams(Map<String, Object> params);
    void plan(
            Table t, String metricCol
    );
}
