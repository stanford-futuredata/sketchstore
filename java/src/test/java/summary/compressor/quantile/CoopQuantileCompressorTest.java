package summary.compressor.quantile;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.junit.Test;
import summary.CDFSketch;

import static org.junit.Assert.*;

public class CoopQuantileCompressorTest {
    @Test
    public void testSimple() {
        CoopQuantileCompressor compress = new CoopQuantileCompressor();
        int n = 1000;
        DoubleArrayList xs = new DoubleArrayList(n);
        for (int i = 0; i < n; i++) {
            xs.add(i);
        }
        for (int j = 0; j < 4; j++) {
            CDFSketch sketch1 = compress.compress(xs, 1);
            System.out.println(sketch1);
        }
    }

}