package sketches;

import org.apache.commons.math3.random.RandomGenerator;
import sketches.counters.IntHeap;
import sketches.counters.KeyCount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

public class ApproxBalancedCounterCompressor<K> implements CounterCompressor<K>
{
  private RandomGenerator rng;
  private int maxError = 0;

  public ApproxBalancedCounterCompressor(RandomGenerator rng) {
    this.rng = rng;
  }

  private int updateEndIdx(int[] bucketCounts, int curEndIdx, int newCount) {
    return 0;
  }

  private int findMinIdx(int[] bucketCounts, int startIdx, int endIdx) {
    int minVal = bucketCounts[startIdx];
    int minIdx = startIdx;
    for (int i = startIdx; i < endIdx; i++) {
      if (bucketCounts[i] < minVal) {
        minVal = bucketCounts[i];
        minIdx = i;
      }
    }
    return minIdx;
  }

  public Collection<KeyCount<K>> compress(Collection<KeyCount<K>> xs, int newSize) {
    IntHeap<K> orderedItems = new IntHeap<>(xs.size());
    orderedItems.setAscending(false);
    for (KeyCount<K> x : xs) {
      orderedItems.offer(x.key, x.count);
    }
    K[] bucketKeys = (K[])new Object[newSize];
    int[] bucketCounts = new int[newSize];
    int fillIdx = 0;
    int exactEndIdx = newSize;
    while (!orderedItems.isEmpty()) {
      KeyCount<K> nextBiggestItem = orderedItems.poll();
      if (fillIdx < newSize) {
        bucketKeys[fillIdx] = nextBiggestItem.key;
        bucketCounts[fillIdx] = nextBiggestItem.count;
        fillIdx++;
      } else {
        int minIdx = findMinIdx(bucketCounts, 0, newSize);
        int combinedWeight = nextBiggestItem.count + bucketCounts[minIdx];
        if (combinedWeight > maxError) {
          maxError = combinedWeight;
        }
        K newKey = bucketKeys[minIdx];
        int randVal = rng.nextInt(combinedWeight);
        if (randVal >= bucketCounts[minIdx]) {
          newKey = nextBiggestItem.key;
        }
        bucketCounts[minIdx] = combinedWeight;
        bucketKeys[minIdx] = newKey;
      }
    }

    ArrayList<KeyCount<K>> finalBuckets = new ArrayList<>(newSize);
    for (int i = 0; i < newSize; i++) {
      finalBuckets.add(new KeyCount<>(bucketKeys[i], bucketCounts[i]));
    }
    return finalBuckets;
  }

  @Override
  public int getMaxError() {
    return maxError;
  }
}
