package sketches;

import org.apache.commons.math3.random.RandomGenerator;
import sketches.counters.KeyCount;

import java.util.ArrayList;
import java.util.Collection;

public class RoundingCounterCompressor<K> implements CounterCompressor<K>
{
  private RandomGenerator rng;
  private int threshold;

  public RoundingCounterCompressor(RandomGenerator rng) {
    this.rng = rng;
  }

  @Override
  public Collection<KeyCount<K>> compress(Collection<KeyCount<K>> xs, int newSize)
  {
    ArrayList<KeyCount<K>> newCounters = new ArrayList<>(2*newSize);
    int totalCount = 0;
    for (KeyCount curEntry : xs) {
      totalCount += curEntry.count;
    }
    threshold = (int)Math.ceil(totalCount*1.0 / newSize);

    for (KeyCount curEntry : xs) {
      if (curEntry.count >= threshold) {
        newCounters.add(curEntry);
      } else {
        int randInt = rng.nextInt(threshold);
        if (curEntry.count > randInt) {
          curEntry.count = threshold;
          newCounters.add(curEntry);
        }
      }
    }
    return newCounters;
  }

  @Override
  public int getMaxError()
  {
    return threshold;
  }
}
