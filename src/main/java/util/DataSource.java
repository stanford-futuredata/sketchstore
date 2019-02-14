package util;

import java.io.IOException;
import java.util.ArrayList;

public interface DataSource {
    default double[] getDouble() throws IOException {
        ArrayList<String> rawValues = get();
        int n = rawValues.size();
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = Double.parseDouble(rawValues.get(i));
        }
        return values;
    }
    default int[] getInt() throws IOException {
        ArrayList<String> rawValues = get();
        int n = rawValues.size();
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = Integer.parseInt(rawValues.get(i));
        }
        return values;
    }
    ArrayList<String> get() throws IOException;
}