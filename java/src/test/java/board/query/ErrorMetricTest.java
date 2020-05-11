package board.query;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.primitive.DoubleLists;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorMetricTest {
    @Test
    public void testSimple() {
        DoubleList x1 = DoubleLists.immutable.of(1.0, 2.0, 3.0);
        DoubleList x2 = DoubleLists.immutable.of(2.0, 4.0, 3.0);
        MutableMap<String, Double> results = ErrorMetric.calcErrors(x1, x2);
        System.out.println(results);
    }

}