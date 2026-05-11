package com.sqlcompiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Table> tables;

    public SymbolTable() {
        tables = new HashMap<>();

        Table usuarios = new Table("usuarios");
        usuarios.addColumn("id", DataType.INT);
        usuarios.addColumn("nombre", DataType.VARCHAR);
        usuarios.addColumn("edad", DataType.INT);
        usuarios.addColumn("ciudad", DataType.VARCHAR);
        tables.put("usuarios", usuarios);

        Table productos = new Table("productos");
        productos.addColumn("id", DataType.INT);
        productos.addColumn("nombre", DataType.VARCHAR);
        productos.addColumn("precio", DataType.FLOAT);
        productos.addColumn("categoria", DataType.VARCHAR);
        tables.put("productos", productos);
    }

    public Table findTable(String tableName) {
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(tableName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void print() {
        System.out.println("========== SCHEMA DE BASE DE DATOS ==========");
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            entry.getValue().print();
            System.out.println();
        }
        System.out.println("=============================================");
    }
}
