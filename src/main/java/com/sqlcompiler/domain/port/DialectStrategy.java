package com.sqlcompiler.domain.port;

public interface DialectStrategy {
    String getDialectName();
    boolean validateSpecificRules(String query);
}
