package sketches;

import sketches.counters.KeyCount;

import java.util.Collection;
import java.util.List;

public interface CounterCompressor<K>
{
  List<KeyCount<K>> compress(List<KeyCount<K>> xs, int newSize);
  int getMaxError();
}
