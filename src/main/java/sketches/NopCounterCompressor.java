package sketches;

import sketches.counters.KeyCount;

import java.util.Collection;
import java.util.List;

public class NopCounterCompressor<K> implements CounterCompressor<K>
{
  @Override
  public List<KeyCount<K>> compress(List<KeyCount<K>> xs, int newSize)
  {
    return xs;
  }

  @Override
  public int getMaxError()
  {
    return 0;
  }
}
