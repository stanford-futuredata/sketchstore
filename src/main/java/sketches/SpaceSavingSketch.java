package sketches;

import sketches.counters.KeyCount;

import java.util.Map;

public class SpaceSavingSketch<K> extends AbstractCounterSketch<K>
{
  private int size;

  SpaceSavingSketch(int size) {
    super();
    this.size = size;
  }

  protected KeyCount<K> getMinItem() {
    int minValue = Integer.MAX_VALUE;
    K minKey = null;
    for (Map.Entry<K, Integer> entry : countMap.entrySet()) {
      K curKey = entry.getKey();
      int curCount = entry.getValue();
      if (curCount < minValue) {
        minValue = curCount;
        minKey = curKey;
      }
    }
    return new KeyCount<>(minKey, minValue);
  }

  @Override
  public void add(K x, int weight)
  {
    if (countMap.containsKey(x)) {
      countMap.put(x, countMap.get(x) + weight);
    } else if (countMap.size() < size) {
      countMap.put(x, weight);
    } else {
      KeyCount<K> oldMinItem = getMinItem();
      countMap.remove(oldMinItem.key);
      countMap.put(x, oldMinItem.count+weight);
    }
  }

  @Override
  public int getError()
  {
    return getMinItem().count;
  }
}
