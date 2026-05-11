package com.sqlcompiler;

import java.util.List;

/**
 * Patron Strategy: cada dialecto SQL tiene su propia validacion.
 * Implementar una clase por dialecto (MySQLStrategy, PostgreSQLStrategy, etc.)
 */
public interface DialectStrategy {
    String getDialectName();
    List<String> getKeywords();
    boolean validateSpecificRules(SelectNode ast, List<String> errors);
}
