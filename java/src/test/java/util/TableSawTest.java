package util;

import org.junit.Test;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;

import static org.junit.Assert.*;

public class TableSawTest {
    @Test
    public void testLoad() throws Exception{
        Table testTable = Table.read().csv("src/test/resources/tiny.csv");
        ColumnType[] ctypes = testTable.columnTypes();
        assertEquals(3, ctypes.length);
        assertEquals(4, testTable.rowCount());
    }
}
