package util;

import java.io.IOException;
import java.util.ArrayList;

public interface DataSource<T> {
    ArrayList<T> get() throws IOException;
}