package sketches;

import sketches.counters.KeyCount;

import java.util.Collection;

public interface CounterCompressor<K>
{
  Collection<KeyCount<K>> compress(Collection<KeyCount<K>> xs, int newSize);
  int getMaxError();
}
