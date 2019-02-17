package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GroupedIntCSVDataSource implements GroupedDataSource<Integer>
{
  public String fileName;
  public int limit = Integer.MAX_VALUE;
  public boolean hasHeader = false;

  public GroupedIntCSVDataSource(String fileName)
  {
    this.fileName = fileName;
  }

  public void setHasHeader(boolean flag)
  {
    this.hasHeader = flag;
  }

  public void setLimit(int limit)
  {
    this.limit = limit;
  }

  @Override
  public ArrayList<ArrayList<Integer>> getGroups() throws IOException
  {
    BufferedReader bf = new BufferedReader(new FileReader(fileName));
    if (hasHeader) {
      bf.readLine();
    }
    ArrayList<ArrayList<Integer>> groups = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      String curLine = bf.readLine();
      if (curLine == null) {
        break;
      }
      String[] rawGroup = curLine.substring(curLine.indexOf('[') + 1, curLine.lastIndexOf(']')).split(",");
      ArrayList<Integer> groupVals = new ArrayList<>(rawGroup.length);
      for (int j = 0; j < rawGroup.length; j++) {
        groupVals.add(Integer.parseInt(rawGroup[j]));
      }
      groups.add(groupVals);
    }
    return groups;
  }
}
