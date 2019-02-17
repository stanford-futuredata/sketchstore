package sketches;

import org.apache.commons.math3.random.RandomGenerator;
import sketches.counters.IntHeap;
import sketches.counters.KeyCount;

import java.util.Collection;
import java.util.PriorityQueue;

public class BalancedCounterCompressor<K> implements CounterCompressor<K>
{
  private RandomGenerator rng;
  private int maxError = 0;

  public BalancedCounterCompressor(RandomGenerator rng) {
    this.rng = rng;
  }

  public Collection<KeyCount<K>> compress(Collection<KeyCount<K>> xs, int newSize) {
    PriorityQueue<KeyCount<K>> orderedItems = new PriorityQueue<>(
        xs.size(),
        (a, b) -> -a.compareTo(b)
    );
    for (KeyCount<K> x : xs) {
      orderedItems.offer(x);
    }
    PriorityQueue<KeyCount<K>> buckets = new PriorityQueue<>(newSize);

    while(!orderedItems.isEmpty()) {
      KeyCount<K> nextBiggestItem = orderedItems.poll();
      if (buckets.size() < newSize) {
        buckets.offer(nextBiggestItem);
      } else {
        KeyCount<K> smallestBucket = buckets.poll();
        int combinedWeight = nextBiggestItem.count + smallestBucket.count;
        if (combinedWeight > maxError) {
          maxError = combinedWeight;
        }
        int randVal = rng.nextInt(combinedWeight);
        if (randVal >= smallestBucket.count) {
          smallestBucket.key = nextBiggestItem.key;
        }
        smallestBucket.count = combinedWeight;
        buckets.offer(smallestBucket);
      }
    }
    return buckets;
  }

  public Collection<KeyCount<K>> compress2(Collection<KeyCount<K>> xs, int newSize) {
    IntHeap<K> orderedItems = new IntHeap<>(xs.size());
    orderedItems.setAscending(false);
    for (KeyCount<K> x : xs) {
      orderedItems.offer(x.key, x.count);
    }
    IntHeap<K> buckets = new IntHeap<>(newSize);

    while(!orderedItems.isEmpty()) {
      KeyCount<K> nextBiggestItem = orderedItems.poll();
      if (buckets.size() < newSize) {
        buckets.offer(nextBiggestItem);
      } else {
        KeyCount<K> smallestBucket = buckets.peek();
        int combinedWeight = nextBiggestItem.count + smallestBucket.count;
        if (combinedWeight > maxError) {
          maxError = combinedWeight;
        }
        K newKey = smallestBucket.key;
        int randVal = rng.nextInt(combinedWeight);
        if (randVal >= smallestBucket.count) {
          newKey = nextBiggestItem.key;
        }
        buckets.replaceMin(newKey, combinedWeight);
      }
    }
    return buckets;
  }

  @Override
  public int getMaxError() {
    return maxError;
  }
}
