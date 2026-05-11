package com.sqlcompiler;

import java.util.regex.Pattern;

/**
 * Detecta automaticamente el dialecto SQL basado en heuristicas.
 */
public class DialectDetector {

    public static String detect(String query) {
        String upper = query.toUpperCase().trim();

        // PostgreSQL: ILIKE, ::type casting, RETURNING
        if (containsAny(upper, "ILIKE", "::", "RETURNING", "ARRAY_AGG")) {
            return "postgresql";
        }

        // MariaDB: CONNECT, no tiene algunas keywords de PG
        if (containsAny(upper, "LIMIT", "OFFSET")) {
            if (!containsAny(upper, "ILIKE", "::")) {
                return "mariadb";
            }
        }

        // MySQL (MariaDB compatible por defecto)
        if (containsAny(upper, "LIMIT", "AUTO_INCREMENT", "ENGINE =")) {
            return "mysql";
        }

        // SQLite
        if (containsAny(upper, "AUTOINCREMENT", "IF NOT EXISTS")) {
            return "sqlite";
        }

        // PL/SQL (Oracle)
        if (containsAny(upper, "DECLARE", "BEGIN", "EXCEPTION", "PLS_INTEGER")) {
            return "plsql";
        }

        // MongoDB (JSON-like query syntax)
        if (containsAny(upper, "find(", "aggregate(", "db.")) {
            return "mongodb";
        }

        return "mysql"; // default
    }

    private static boolean containsAny(String text, String... values) {
        for (String v : values) {
            if (text.contains(v)) return true;
        }
        return false;
    }
}
