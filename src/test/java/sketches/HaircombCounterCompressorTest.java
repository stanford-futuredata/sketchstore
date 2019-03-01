package sketches;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;
import sketches.counters.KeyCount;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class HaircombCounterCompressorTest {
    @Test
    public void testSimple() {
        List<KeyCount<Integer>> xs = Arrays.asList(
                new KeyCount<>(1, 10),
                new KeyCount<>(2, 10),
                new KeyCount<>(3, 5),
                new KeyCount<>(4, 5)
        );
        int sketchSize = 3;
        CounterCompressor<Integer> bc = new HaircombCounterCompressor<>(
                new MersenneTwister(0)
        );
        Collection<KeyCount<Integer>> buckets = bc.compress(xs, sketchSize);
        assertEquals(sketchSize, buckets.size());
        System.out.println(buckets);
        System.out.println(bc.getMaxError());
    }
}