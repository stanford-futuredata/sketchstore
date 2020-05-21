package io;

import board.StoryBoard;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IOUtil {
    public static String getBoardName(
            String sketch,
            int size,
            int granularity
    ) {
        return String.format(
                "board-%s-%d-%d.out",
                sketch,
                size,
                granularity
        );
    }

    public static Serializable testSerDe(Serializable o) throws IOException, ClassNotFoundException {
        File tempFile = File.createTempFile("board_serialize", "temp");
        tempFile.deleteOnExit();
        FileOutputStream fOut = new FileOutputStream(tempFile);
        ObjectOutputStream oOut = new ObjectOutputStream(fOut);
        oOut.writeObject(o);
        oOut.close();
        FileInputStream fIn = new FileInputStream(tempFile);
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        return (Serializable)oIn.readObject();
    }


    public static <T> void writeBoard(StoryBoard<T> board, File f) throws IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        ObjectOutputStream oOut = new ObjectOutputStream(fOut);
        oOut.writeObject(board);
        oOut.close();
    }

    public static <T> StoryBoard<T> loadBoard(File f) throws IOException, ClassNotFoundException {
        FileInputStream fIn = new FileInputStream(f);
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        StoryBoard<T> boardIn = (StoryBoard<T>)oIn.readObject();
        return boardIn;
    }

    public static Table loadTable(String path, List<Integer> colTypes) throws IOException {
        int d = colTypes.size();
        ColumnType[] columnTypes = new ColumnType[d];
        for (int i = 0; i < d; i++) {
            int curTypeIdx = colTypes.get(i);
            if (curTypeIdx == 0) {
                columnTypes[i] = ColumnType.LONG;
            } else {
                columnTypes[i] = ColumnType.DOUBLE;
            }
        }

        Table t = Table.read().usingOptions(CsvReadOptions
                .builder(path)
                .columnTypes(columnTypes));
        return t;
    }

    public static void writeAllResults(
            List<Map<String, String>> results,
            File file
    ) throws Exception {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));

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
