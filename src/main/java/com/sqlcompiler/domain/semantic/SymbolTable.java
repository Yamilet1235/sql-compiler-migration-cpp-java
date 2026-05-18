package com.sqlcompiler.domain.semantic;

import java.util.HashMap;
import java.util.Map;

import com.sqlcompiler.domain.model.DataType;
import com.sqlcompiler.domain.model.Table;
import com.sqlcompiler.domain.port.SchemaRepositoryPort;

public class SymbolTable {

    private SchemaRepositoryPort schemaRepository;
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

    public SymbolTable(SchemaRepositoryPort schemaRepository) {
        this.schemaRepository = schemaRepository;
        this.tables = new HashMap<>();
    }

    public Table findTable(String tableName) {
        if (schemaRepository != null) {
            return schemaRepository.findTable(tableName);
        }

        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(tableName)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public void loadSchema(String jsonSchema) {
        if (schemaRepository != null) {
            schemaRepository.loadSchema(jsonSchema);
        }
    }
}


