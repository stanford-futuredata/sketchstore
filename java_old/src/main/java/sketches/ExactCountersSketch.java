package sketches;

public class ExactCountersSketch<K> extends AbstractCounterSketch<K>
{
  private int maxError = 0;

  public ExactCountersSketch()
  {
    super();
  }

  @Override
  public void add(K x, int weight)
  {
    int oldCount = countMap.getOrDefault(x, 0);
    countMap.put(x, oldCount + weight);
  }

  @Override
  public void merge(FrequentItemsSketch<K> other) {
    super.merge(other);
    maxError = Math.max(maxError, other.getError());
  }

  @Override
  public int getError()
  {
    return maxError;
  }
}
