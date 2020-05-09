package io;

public class SimpleCSVDataSourceDouble extends SimpleCSVDataSource<Double> {
    public SimpleCSVDataSourceDouble(String fileName, int column) {
        super(fileName, column);
    }

    @Override
    public Double parseString(String strVal) {
        return Double.parseDouble(strVal);
    }
}
