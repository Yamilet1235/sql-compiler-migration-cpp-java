package com.sqlcompiler;

    public enum CompOperator {
    EQUAL,
    GREATER,
    LESS,
    GREATER_EQUAL,
    LESS_EQUAL,
    NOT_EQUAL;

    public static String toString(CompOperator op) {
        switch (op) {
            case EQUAL:         return "=";
            case GREATER:       return ">";
            case LESS:          return "<";
            case GREATER_EQUAL: return ">=";
            case LESS_EQUAL:    return "<=";
            case NOT_EQUAL:     return "!=";
            default:            return "?";
        }
    }
}

