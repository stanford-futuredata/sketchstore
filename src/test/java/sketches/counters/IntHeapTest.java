package sketches.counters;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntHeapTest
{
  @Test
  public void testReversed() {
    IntHeap<Integer> iHeap = new IntHeap<Integer>(10);
    iHeap.setAscending(false);
    int[] xs = {5, 4, 6, 3, 2, 1, 9, 8, 7, 0};
    for (Integer x : xs) {
      iHeap.offer(x, x);
    }
    assertEquals(9, iHeap.peek().count);
  }

  @Test
  public void testSimple() {
    IntHeap<Integer> iHeap = new IntHeap<>(10);
    int[] xs = {5, 4, 6, 3, 2, 1, 9, 8, 7, 0};
    for (Integer x : xs) {
      iHeap.offer(x, x);
    }
    assertEquals(0, iHeap.peek().count);

    KeyCount<Integer> nextSmallest = new KeyCount<>(0,0);
    while(!iHeap.isEmpty()) {
      nextSmallest = iHeap.poll();
    }
    assertEquals(9, nextSmallest.count);
    assertEquals(9, nextSmallest.key.intValue());
  }

  @Test
  public void testReplaceMin() {
    IntHeap<Integer> iHeap = new IntHeap<>(10);
    int[] xs = {5, 4, 6, 3, 2, 1, 9, 8, 7, 0};
    KeyCount<Integer> tempTuple = new KeyCount<>(0, 0);
    for (Integer x : xs) {
      iHeap.offer(x, x);
    }
    iHeap.replaceMin(33, 3);
    KeyCount<Integer> newSmallest = iHeap.peek();
    assertEquals(1, newSmallest.count);
  }
}