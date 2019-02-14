package sketches.counters;

import java.util.Objects;

public class KeyCount<T> implements Comparable<KeyCount>
{
  public T key;
  public int count;

  public KeyCount(T x, int y) {
    key = x;
    count = y;
  }

  @Override
  public String toString() {
    return key.toString()+"->"+count;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyCount keyCount = (KeyCount) o;
    return count == keyCount.count &&
           Objects.equals(key, keyCount.key);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(key, count);
  }

  @Override
  public int compareTo(KeyCount o)
  {
    return count - o.count;
  }
}
