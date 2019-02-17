package util;

import java.io.IOException;
import java.util.ArrayList;

public interface GroupedDataSource<T>
{
  ArrayList<ArrayList<T>> getGroups() throws IOException;
}
