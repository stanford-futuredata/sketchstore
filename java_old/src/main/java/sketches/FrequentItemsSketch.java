package sketches;

import sketches.counters.KeyCount;

import java.util.Collection;
import java.util.List;

public interface FrequentItemsSketch<K>
{
  void add(K x, int weight);
  default void add(K x) {
    add(x, 1);
  }
  default void add(Collection<KeyCount<K>> xs) {
    for (KeyCount<K> x : xs) {
      add(x.key, x.count);
    }
  }
  List<KeyCount<K>> getItems();
  int getCount(K x);
  int getTotal();

  void merge(FrequentItemsSketch<K> other);

  int getError();
  int getSize();
}
