package io;

public class SimpleCSVDataSourceDouble extends SimpleCSVDataSource<Double> {
    @Override
    public Double parseString(String strVal) {
        return Double.parseDouble(strVal);
    }
}
