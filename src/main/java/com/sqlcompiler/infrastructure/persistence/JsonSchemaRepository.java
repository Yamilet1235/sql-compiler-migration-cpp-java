package com.sqlcompiler.infrastructure.persistence;

import com.sqlcompiler.domain.model.Table;
import com.sqlcompiler.domain.port.SchemaRepositoryPort;

public class JsonSchemaRepository implements SchemaRepositoryPort {

    @Override
    public Table findTable(String tableName) {
        return null;
    }

    @Override
    public void loadSchema(String jsonSchema) {
    }
}
