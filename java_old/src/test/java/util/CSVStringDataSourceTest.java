package util;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CSVStringDataSourceTest
{
  @Test
  public void testLoadCSV2() throws Exception
  {
    CSVStringDataSource s = new CSVStringDataSource(
        "src/test/resources/tiny.csv",
        0
    );
    s.setHasHeader(true);
    ArrayList<String> col = s.get();
    assertEquals("1", col.get(0));
  }
}