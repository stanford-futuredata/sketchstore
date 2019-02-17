package util;

public class CSVStringDataSource extends AbstractCSVDataSource<String> implements DataSource<String>
{
  public CSVStringDataSource(String fileName, int column)
  {
    super(fileName, column);
  }

  @Override
  String parse(String val)
  {
    return val;
  }
}
