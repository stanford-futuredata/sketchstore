package util;

import org.junit.Test;
import util.SimpleCSVDataSource;

import static org.junit.Assert.*;

public class SimpleCSVDataSourceTest
{
  @Test
  public void testLoadCSV2() throws Exception
  {
    SimpleCSVDataSource s = new SimpleCSVDataSource(
        "src/test/resources/tiny.csv",
        0
    );
    s.setHasHeader(true);
    int[] col = s.getInt();
    assertEquals(1, col[0]);
  }
}