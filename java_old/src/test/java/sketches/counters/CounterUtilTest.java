package sketches.counters;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CounterUtilTest {
    @Test
    public void testSimple() {
        List<KeyCount<Integer>> xs = new ArrayList<>();
        int[] keys = {4, 3, 2, 1};
        int[] vals = {20, 30, 50, 100};
        for (int i = 0; i < keys.length; i++) {
            xs.add(new KeyCount<>(keys[i], vals[i]));
        }
        CounterUtil<Integer> cu = new CounterUtil<>(xs);
        cu.process(3);
        assertEquals(1, xs.get(0).key.intValue());
        assertEquals(50.0, cu.getThreshold(), 1e-10);

        cu.process(10);
        assertEquals(0.0, cu.getThreshold(), 1e-10);
        assertEquals(4, cu.getTailIdx());
    }
}