package com.sqlcompiler.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private String name;
    private List<Column> columns;

    public Table(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void addColumn(String name, DataType type) {
        columns.add(new Column(name, type));
    }

    public Column findColumn(String columnName) {
        for (Column col : columns) {
            if (col.getName().equalsIgnoreCase(columnName)) {
                return col;
            }
        }
        return null;
    }

    public void print() {
        System.out.println("Tabla: " + name);
        for (Column col : columns) {
            System.out.println(col);
        }
    }
}
