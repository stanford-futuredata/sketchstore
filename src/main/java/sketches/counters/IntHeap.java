package sketches.counters;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;

public class IntHeap<K> extends AbstractQueue<KeyCount<K>>
{
  private K[] keys;
  private int[] counts;
  private int size;
  private boolean ascending = true;

  public IntHeap(int initialCapacity) {
    keys = (K[]) new Object[initialCapacity];
    counts = new int[initialCapacity];
    size = 0;
  }

  public IntHeap<K> setAscending(boolean flag) {
    this.ascending = flag;
    return this;
  }

  protected void expand(int newCapacity) {
    K[] newKeys = Arrays.copyOf(keys, newCapacity);
    int[] newCounts = Arrays.copyOf(counts, newCapacity);
    keys = newKeys;
    counts = newCounts;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean offer(KeyCount<K> item) {
    return offer(item.key, item.count);
  }

  public boolean offer(K newKey, int newCount) {
    append(newKey, newCount);
    heapUp(size-1);
    return true;
  }

  @Override
  public KeyCount<K> poll() {
    if (size == 0) {
      return null;
    } else {
      KeyCount<K> firstElt = new KeyCount<>(keys[0], counts[0]);
      keys[0] = null;
      counts[0] = 0;
      swap(0, size-1);
      size--;
      heapDown(0);
      return firstElt;
    }
  }

  @Override
  public KeyCount<K> peek()
  {
    return new KeyCount<>(keys[0], counts[0]);
  }

  public void replaceMin(K newKey, int newCount) {
    keys[0] = newKey;
    counts[0] = newCount;
    heapDown(0);
  }

  private void swap(int i, int j) {
    K iK = keys[i];
    int iC = counts[i];
    keys[i] = keys[j];
    counts[i] = counts[j];
    keys[j] = iK;
    counts[j] = iC;
  }

  private void append(K x, int c) {
    if (size >= keys.length) {
      expand(keys.length*2);
    }
    keys[size] = x;
    counts[size] = c;
    size++;
  }

  private void heapDown(int ogIdx) {
    K ogKey = keys[ogIdx];
    int ogCount = counts[ogIdx];

    int curIdx = ogIdx;
    int idxToSwap = curIdx*2+1;
    while(idxToSwap < size) {
      if (idxToSwap+1 < size && compareVal(idxToSwap, idxToSwap + 1) > 0) {
        idxToSwap++;
      }
      if (compareVal(idxToSwap, curIdx) < 0) {
        keys[curIdx] = keys[idxToSwap];
        counts[curIdx] = counts[idxToSwap];
        curIdx = idxToSwap;
        idxToSwap = curIdx*2+1;
      } else {
        break;
      }
    }
    keys[curIdx] = ogKey;
    counts[curIdx] = ogCount;
  }
  private void heapUp(int curIdx) {
    while (curIdx > 0) {
      int parentIdx = (curIdx - 1)/2;
      if (compareVal(curIdx, parentIdx) < 0){
        swap(curIdx, parentIdx);
        curIdx = parentIdx;
      } else {
        break;
      }
    }
  }
  private int compareVal(int i, int j) {
    int diff = counts[i] - counts[j];
    if (ascending) {
      return diff;
    } else {
      return -diff;
    }
  }

  public class IntHeapIterator<K> implements Iterator<KeyCount<K>> {
    private IntHeap<K> heap;
    private int idx;

    public IntHeapIterator(IntHeap<K> heap) {
      this.heap = heap;
      this.idx = 0;
    }

    @Override
    public boolean hasNext()
    {
      return idx < heap.size();
    }

    @Override
    public KeyCount<K> next()
    {
      KeyCount<K> nextval =  new KeyCount<>(heap.keys[idx], heap.counts[idx]);
      idx++;
      return nextval;
    }
  }

  @Override
  public Iterator<KeyCount<K>> iterator()
  {
    return new IntHeapIterator<>(this);
  }

  @Override
  public int size()
  {
    return size;
  }
}
