package com.sqlcompiler.domain.semantic;

public class DialectDetector {

   public static String detect(String query) {
        String upper = query.toUpperCase().trim();

        if (upper.startsWith("DB.") || upper.contains(".FIND(") || upper.contains(".AGGREGATE(")) {
            return "mongodb";
        }
        if (containsAny(upper, "ILIKE", "::", "RETURNING", "ARRAY_AGG")) {
            return "postgresql";
        }
        if (containsAny(upper, "TOP", "GETDATE", "NVARCHAR", "IDENTITY")) {
            return "sqlserver";
        }
        if (containsAny(upper, "AUTO_INCREMENT", "ENGINE =", "LIMIT", "SHOW TABLES", "DESCRIBE", "NEXT VALUE FOR", "SQL_CALC_FOUND_ROWS", "CREATE SEQUENCE")) {
            return "mysql";
        }
        return "mysql";
    }
    private static boolean containsAny(String text, String... VALUES){
        for(String v: VALUES){
            if (text.contains(v)) return true;
        }
        return false;

    }
}
