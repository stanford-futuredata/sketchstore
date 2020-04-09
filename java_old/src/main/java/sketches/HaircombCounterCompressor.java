package sketches;

import org.apache.commons.math3.random.RandomGenerator;
import sketches.counters.CounterUtil;
import sketches.counters.KeyCount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HaircombCounterCompressor<K> implements CounterCompressor<K>
{
  private RandomGenerator rng;
  private int threshold;
  private boolean useRawCount = false;

  public HaircombCounterCompressor(RandomGenerator rng) {
    this.rng = rng;
  }
  public void setUseRawCount(boolean flag) {
    useRawCount = flag;
  }

  @Override
  public List<KeyCount<K>> compress(List<KeyCount<K>> xs, int newSize)
  {
    CounterUtil<K> cu = new CounterUtil<>(xs);
    cu.process(newSize);
    threshold = (int)Math.ceil(cu.getThreshold());
    int tailIdx = cu.getTailIdx();

    ArrayList<KeyCount<K>> newCounters = new ArrayList<>(newSize);
    for (int i = 0; i < tailIdx; i++) {
      newCounters.add(xs.get(i));
    }

    double randShift = rng.nextDouble()*threshold;
    double runningSum = 0;
    for (int i = tailIdx; i < xs.size(); i++) {
      KeyCount<K> curEntry = xs.get(i);
      runningSum += curEntry.count;
      if (runningSum > randShift) {
        runningSum -= threshold;
        if (!useRawCount) {
          curEntry.count = threshold;
        }
        newCounters.add(curEntry);
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
