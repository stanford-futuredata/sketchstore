package util;

import java.io.IOException;
import java.util.ArrayList;

public class GroupedSeqDataSource<T> implements GroupedDataSource<T>
{
  private int numGroups;
  private ArrayList<T> rawData;

  public GroupedSeqDataSource(int numGroups, ArrayList<T> rawData)
  {
    this.numGroups = numGroups;
    this.rawData = rawData;
  }

  @Override
  public ArrayList<ArrayList<T>> getGroups() throws IOException
  {
    int n = rawData.size();
    int cellSize = (int) Math.ceil(n * 1.0 / numGroups);
    ArrayList<ArrayList<T>> cells = new ArrayList<>(numGroups);

    for (int i = 0; i < numGroups; i++) {
      int startIdx = i * cellSize;
      int endIdx = Math.min((i + 1) * cellSize, n);
      ArrayList<T> curCell = new ArrayList<>(endIdx - startIdx);
      for (int j = startIdx; j < endIdx; j++) {
        curCell.add(rawData.get(j));
      }
      cells.add(curCell);
    }
    return cells;
  }
}
