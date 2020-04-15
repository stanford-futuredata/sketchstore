package board;

import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import org.junit.Test;
import summary.DictSketch;

import java.io.*;

import static org.junit.Assert.*;

public class SketchBoardTest {
    @Test
    public void testSerialize() throws IOException, ClassNotFoundException {
        SketchBoard<Long> board = new SketchBoard<>(1);

        DictSketch sketch = new DictSketch(
                LongDoubleHashMap.newWithKeysValues(3, 4.0)
        );
        board.addSketch(LongArrayList.newListWith(1), sketch);

        File tempFile = File.createTempFile("board_serialize", "temp");
//        System.out.println(tempFile.getAbsolutePath());
        tempFile.deleteOnExit();
//        System.out.println(tempFile.length());

        FileOutputStream fOut = new FileOutputStream(tempFile);
        ObjectOutputStream oOut = new ObjectOutputStream(fOut);
        oOut.writeObject(board);
        oOut.close();
//        System.out.println(tempFile.length());
        FileInputStream fIn = new FileInputStream(tempFile);
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        SketchBoard<Long> boardIn = (SketchBoard<Long>)oIn.readObject();
        assertEquals(
                board.sketchCol.get(0).estimate(3L),
                boardIn.sketchCol.get(0).estimate(3L),
                1e-10
        );
        assertEquals(
                board.dimensionCols.get(0).get(0),
                boardIn.dimensionCols.get(0).get(0)
        );
    }
}