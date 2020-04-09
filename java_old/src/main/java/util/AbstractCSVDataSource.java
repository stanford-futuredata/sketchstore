package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public abstract class AbstractCSVDataSource<T> implements DataSource<T>
{
  public String fileName;
  public int column;
  public int limit = Integer.MAX_VALUE;
  public boolean hasHeader = true;

  public AbstractCSVDataSource(String fileName, int column)
  {
    this.fileName = fileName;
    this.column = column;
  }
  public void setHasHeader(boolean flag)
  {
    this.hasHeader = flag;
  }

  public void setLimit(int limit)
  {
    this.limit = limit;
  }

  abstract T parse(String val);

  @Override
  public ArrayList<T> get() throws IOException
  {
    BufferedReader bf = new BufferedReader(new FileReader(fileName));
    if (hasHeader) {
      bf.readLine();
    }
    ArrayList<T> vals = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      String curLine = bf.readLine();
      if (curLine == null) {
        break;
      }
      int colCount = 0;
      int startIdx = 0;
      int nextIdx = -1;
      while (colCount <= column) {
        startIdx = nextIdx + 1;
        nextIdx = curLine.indexOf(',', startIdx);
        colCount++;
        if (nextIdx == -1) {
          nextIdx = curLine.length();
          break;
        }
      }
      vals.add(parse(curLine.substring(startIdx, nextIdx)));
    }
    return vals;
  }
}
