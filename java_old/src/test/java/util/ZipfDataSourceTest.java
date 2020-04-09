package util;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ZipfDataSourceTest
{
  @Test
  public void testDistribution() throws IOException
  {
    ZipfDataSource zf = new ZipfDataSource(1.3, 0, 100000);
    ArrayList<Integer> xs = zf.get();
    int n_to_check = 2;
    int[] counts = new int[n_to_check+1];
    for (int x : xs) {
      if (x <= n_to_check) {
        counts[x]++;
      }
    }
    assertEquals(25300, counts[1], 1000);
    assertEquals(10400, counts[2], 1000);
  }
}