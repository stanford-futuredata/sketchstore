package summary.compressor.freq;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.util.Pair;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.primitive.LongDoublePair;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;

import java.util.Random;

public class USampleFreqCompressor implements ItemDictCompressor{
    public RandomGenerator rng;

    public USampleFreqCompressor(int seed) {
        rng = RandomGeneratorFactory.createRandomGenerator(new Random(seed));
    }

    @Override
    public LongDoubleHashMap compress(LongDoubleHashMap xs, int size) {
        int nItems = xs.size();
        if (nItems == 0) {
            return xs;
        }

        FastList<Pair<Long, Double>> pmf = new FastList<>(nItems);
        xs.forEachKeyValue((long k, double v) -> {
            pmf.add(Pair.create(k, v));
        });
        EnumeratedDistribution<Long> xDist = new EnumeratedDistribution<>(rng, pmf);

        LongDoubleHashMap newMap = new LongDoubleHashMap(size);
        double totalWeight = xs.sum();
        double weightPerSample = totalWeight/size;
        for (int i = 0; i < size; i++) {
            newMap.addToValue(xDist.sample(), weightPerSample);
        }
        return newMap;
    }
}
