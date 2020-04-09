package sketches;

import sketches.counters.KeyCount;

import java.util.*;

public abstract class AbstractCounterSketch<K> implements FrequentItemsSketch<K>
{
  protected Map<K, Integer> countMap;

  public AbstractCounterSketch() {
    countMap = new HashMap<>();
  }

  @Override
  public List<KeyCount<K>> getItems()
  {
    ArrayList<KeyCount<K>> itemList = new ArrayList<>(countMap.size());
    for (Map.Entry<K, Integer> entry : countMap.entrySet()) {
      itemList.add(new KeyCount<>(entry.getKey(), entry.getValue()));
    }
    return itemList;
  }

  @Override
  public int getCount(K x)
  {
    return countMap.getOrDefault(x, 0);
  }

  @Override
  public int getTotal()
  {
    int total = 0;
    for (int curCount : countMap.values()) {
      total += curCount;
    }
    return total;
  }

  @Override
  public void merge(FrequentItemsSketch<K> other)
  {
    for (KeyCount<K> item : other.getItems()) {
      add(item.key, item.count);
    }
  }

  @Override
  public int getSize()
  {
    return countMap.size();
  }

  @Override
  public String toString() {
    return countMap.toString();
  }
}
