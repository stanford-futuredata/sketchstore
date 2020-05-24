package summary.compressor.quantile;

import org.eclipse.collections.api.list.primitive.DoubleList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.DoubleDoubleHashMap;
import org.eclipse.collections.impl.map.sorted.mutable.TreeSortedMap;
import summary.CounterDoubleSketch;

import java.util.Random;
import java.util.TreeMap;

public class USampleQuantCompressor implements SeqCounterCompressor {
    Random rng;

    public USampleQuantCompressor(int seed) {
        rng = new Random(seed);
    }

    @Override
    public CounterDoubleSketch compress(DoubleList xs, int size) {
        DoubleDoubleHashMap savedValues = new DoubleDoubleHashMap();

        int n = xs.size();
        double savedWeight = n*1.0/size;
        for (int i = 0; i < size; i++) {
            double toSave = xs.get(rng.nextInt(n));
            savedValues.addToValue(toSave, savedWeight);
        }
        double[] sortedSavedItems = savedValues.keysView().toSortedArray();
        double[] savedItemWeights = new double[sortedSavedItems.length];
        for (int i = 0; i < sortedSavedItems.length; i++) {
            savedItemWeights[i] = savedValues.get(sortedSavedItems[i]);
        }
        return new CounterDoubleSketch(
                sortedSavedItems,
                savedItemWeights
        );
    }
}
