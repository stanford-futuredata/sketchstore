package runner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CSVOutput {
    public static void writeAllResults(
            List<Map<String, String>> results,
            String filePath
    ) throws Exception {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));

        List<String> keys = new ArrayList<>(results.get(0).keySet());
        out.println(String.join(",", keys));
        for (Map<String, String> row : results) {
            List<String> vals = new ArrayList<>(keys.size());
            for (String key : keys) {
                vals.add(row.get(key));
            }
            out.println(String.join(",", vals));
        }
        out.close();
    }
}
