package runner;

import board.SketchBoard;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.*;
import java.util.List;

public class IOUtil {
    public static <T> void writeBoard(SketchBoard<T> board, File f) throws IOException {
        FileOutputStream fOut = new FileOutputStream(f);
        ObjectOutputStream oOut = new ObjectOutputStream(fOut);
        oOut.writeObject(board);
        oOut.close();
    }

    public static <T> SketchBoard<T> loadBoard(File f) throws IOException, ClassNotFoundException {
        FileInputStream fIn = new FileInputStream(f);
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        SketchBoard<T> boardIn = (SketchBoard<T>)oIn.readObject();
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
}
