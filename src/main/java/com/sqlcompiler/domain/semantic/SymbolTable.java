package com.sqlcompiler.domain.semantic;

import com.sqlcompiler.domain.model.Table;
import com.sqlcompiler.domain.port.SchemaRepositoryPort;

public class SymbolTable {

    private final SchemaRepositoryPort schemaRepository;

    public SymbolTable(SchemaRepositoryPort schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public Table findTable(String tableName) {

        if (schemaRepository == null) {
            return null;
        }

        return schemaRepository.findTable(tableName);
    }

    public void loadSchema(String jsonSchema) {

        if (schemaRepository != null) {
            schemaRepository.loadSchema(jsonSchema);
        }
    }
}

