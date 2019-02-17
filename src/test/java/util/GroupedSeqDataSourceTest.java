package util;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class GroupedSeqDataSourceTest
{
  @Test
  public void testSimple() throws IOException
  {
    DataSource<Integer> ds = new ZipfDataSource(1.3, 0, 100);
    int numGroups = 9;
    GroupedSeqDataSource<Integer> gs = new GroupedSeqDataSource<>(numGroups, ds.get());
    ArrayList<ArrayList<Integer>> groups = gs.getGroups();
    assertEquals(numGroups, groups.size());

    int totalElements = 0;
    for (ArrayList<Integer> curGroup : groups) {
      totalElements += curGroup.size();
    }
    assertEquals(100, totalElements);
  }

}