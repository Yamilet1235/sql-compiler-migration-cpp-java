package com.sqlcompiler;

public enum DataType {
    INT,
    VARCHAR,
    FLOAT;

    @Override
    public String toString() {
        switch (this) {
            case INT:     return "INT";
            case VARCHAR: return "VARCHAR";
            case FLOAT:   return "FLOAT";
            default:      return "UNKNOWN";
        }
    }
}
