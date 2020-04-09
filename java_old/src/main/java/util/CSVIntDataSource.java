package util;

public class CSVIntDataSource extends AbstractCSVDataSource<Integer> implements DataSource<Integer>
{
  public CSVIntDataSource(String fileName, int column)
  {
    super(fileName, column);
  }

  @Override
  Integer parse(String val)
  {
    return Integer.parseInt(val);
  }
}
