package com.sqlcompiler.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidationRequest {
    @NotBlank(message = "La consulta SQL es obligatoria")
    private String query;

    private String dialect = "mysql";

    private String schemaJson;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getDialect() { return dialect; }
    public void setDialect(String dialect) { this.dialect = dialect; }

    public String getSchemaJson() { return schemaJson; }
    public void setSchemaJson(String schemaJson) { this.schemaJson = schemaJson; }
}
