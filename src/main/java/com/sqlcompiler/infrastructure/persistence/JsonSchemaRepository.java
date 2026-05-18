package com.sqlcompiler.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlcompiler.domain.model.DataType;
import com.sqlcompiler.domain.model.Table;
import com.sqlcompiler.domain.port.SchemaRepositoryPort;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JsonSchemaRepository implements SchemaRepositoryPort {

    private final Map<String, Table> tables = new HashMap<>();

    private String dialect;
    private String database;

    @Override
    public Table findTable(String tableName) {

        if (tableName == null) {
            return null;
        }

        return tables.get(tableName.toLowerCase());
    }

    @Override
    public void loadSchema(String jsonSchema) {

        try {

            tables.clear();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonSchema);

            // DIALECTO
            if (root.has("dialect")) {
                dialect = root.get("dialect").asText();
            }

            // BASE DE DATOS
            if (root.has("database")) {
                database = root.get("database").asText();
            }

            JsonNode tablesNode = root.get("tables");

            if (tablesNode == null || !tablesNode.isObject()) {
                throw new IllegalArgumentException(
                        "El schema debe contener un objeto llamado 'tables'"
                );
            }

            Iterator<String> tableNames = tablesNode.fieldNames();

            while (tableNames.hasNext()) {

                String tableName = tableNames.next();

                Table table = new Table(tableName);

                JsonNode columnsNode =
                        tablesNode.get(tableName).get("columns");

                if (columnsNode != null && columnsNode.isObject()) {

                    Iterator<String> columnNames =
                            columnsNode.fieldNames();

                    while (columnNames.hasNext()) {

                        String columnName = columnNames.next();

                        String typeText =
                                columnsNode.get(columnName).asText();

                        DataType type =
                                DataType.valueOf(typeText.toUpperCase());

                        table.addColumn(columnName, type);
                    }
                }

                tables.put(tableName.toLowerCase(), table);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error cargando schema JSON: " + e.getMessage(),
                    e
            );
        }
    }

    public String getDialect() {
        return dialect;
    }

    public String getDatabase() {
        return database;
    }
}
