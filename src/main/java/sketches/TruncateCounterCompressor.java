package sketches;

import sketches.counters.IntHeap;
import sketches.counters.KeyCount;

import java.util.ArrayList;
import java.util.List;

public class TruncateCounterCompressor<K> implements CounterCompressor<K>
{
  private int threshold = 0;

  @Override
  public List<KeyCount<K>> compress(List<KeyCount<K>> xs, int newSize)
  {
    IntHeap<K> orderedItems = new IntHeap<>(xs.size());
    orderedItems.setAscending(false);
    for (KeyCount<K> x : xs) {
      orderedItems.offer(x.key, x.count);
    }

    ArrayList<KeyCount<K>> newCounters = new ArrayList<>(newSize);
    while (newCounters.size() < newSize && !orderedItems.isEmpty()) {
      newCounters.add(orderedItems.poll());
    }
    if (orderedItems.isEmpty()) {
      threshold = 0;
    } else {
      threshold = orderedItems.peek().count;
    }
    return newCounters;
  }

  @Override
  public int getMaxError()
  {
    return threshold;
  }
}
