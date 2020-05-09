package io;

public class SimpleCSVDataSourceLong extends SimpleCSVDataSource<Long> {
    public SimpleCSVDataSourceLong(String fileName, int column) {
        super(fileName, column);
    }

    @Override
    public Long parseString(String strVal) {
        return Long.parseLong(strVal);
    }
}
