package io;

public class SimpleCSVDataSourceLong extends SimpleCSVDataSource<Long> {
    @Override
    public Long parseString(String strVal) {
        return Long.parseLong(strVal);
    }
}
