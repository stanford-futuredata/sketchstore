package util;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class GroupedIntCSVDataSourceTest
{
  @Test
  public void testSimple() throws IOException {
    GroupedIntCSVDataSource ds = new GroupedIntCSVDataSource(
        "src/test/resources/groups.csv"
    );
    ArrayList<ArrayList<Integer>> groups = ds.getGroups();
    assertEquals(2, groups.size());
  }
}