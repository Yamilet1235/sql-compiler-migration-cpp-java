package com.sqlcompiler.domain.model;

public enum DataType {

    // NUMÉRICOS
    INT,
    INTEGER,
    BIGINT,
    SMALLINT,

    FLOAT,
    DOUBLE,
    DECIMAL,
    NUMERIC,

    // TEXTO
    VARCHAR,
    CHAR,
    TEXT,

    // BOOLEANOS
    BOOLEAN,
    BOOL,

    // FECHAS
    DATE,
    DATETIME,
    TIMESTAMP,
    TIME,

    // MONGODB / JSON
    OBJECTID,
    DOCUMENT,
    ARRAY,
    JSON;

    @Override
    public String toString() {
        return this.name();
    }
}

