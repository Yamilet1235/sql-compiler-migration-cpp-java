package com.sqlcompiler.domain.port;

import com.sqlcompiler.domain.model.Table;

public interface SchemaRepositoryPort {
    Table findTable(String tableName);
    void loadSchema(String jsonSchema);
}
