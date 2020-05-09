package io;

import org.eclipse.collections.impl.list.mutable.FastList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public abstract class SimpleCSVDataSource<T> {
    public String fileName;
    public int column;
    public int limit = Integer.MAX_VALUE;
    public boolean hasHeader = true;

    public SimpleCSVDataSource(String fileName, int column) {
        this.fileName = fileName;
        this.column = column;
    }

    public void setHasHeader(boolean flag) {
        this.hasHeader = flag;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public abstract T parseString(String strVal);

    public FastList<T> get() throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(fileName));
        if (hasHeader) {
            bf.readLine();
        }
        FastList<T> vals = new FastList<>();
        for (int i = 0; i < limit; i++) {
            String curLine = bf.readLine();
            if (curLine == null) {
                break;
            }
            int colCount = 0;
            int startIdx = 0;
            int nextIdx = -1;
            while (colCount <= column) {
                startIdx = nextIdx+1;
                nextIdx = curLine.indexOf(',', startIdx);
                colCount++;
                if (nextIdx == -1) {
                    nextIdx = curLine.length();
                    break;
                }
            }
            vals.add(parseString(curLine.substring(startIdx, nextIdx)));
        }
        return vals;
    }
}
