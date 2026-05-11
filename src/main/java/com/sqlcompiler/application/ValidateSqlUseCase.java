package com.sqlcompiler.application;

public class ValidateSqlUseCase {

    public ValidationResult execute(ValidationCommand command) {
        return new ValidationResult();
    }

    public static class ValidationCommand {
        private String query;
        private String dialect;
        private String schemaJson;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getDialect() { return dialect; }
        public void setDialect(String dialect) { this.dialect = dialect; }
        public String getSchemaJson() { return schemaJson; }
        public void setSchemaJson(String schemaJson) { this.schemaJson = schemaJson; }
    }

    public static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors;
        private java.util.List<String> warnings;
        private String ast;
        private java.util.List<TokenInfo> tokens;

        public ValidationResult() {
            this.valid = true;
            this.errors = new java.util.ArrayList<>();
            this.warnings = new java.util.ArrayList<>();
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public java.util.List<String> getErrors() { return errors; }
        public void setErrors(java.util.List<String> errors) { this.errors = errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        public void setWarnings(java.util.List<String> warnings) { this.warnings = warnings; }
        public String getAst() { return ast; }
        public void setAst(String ast) { this.ast = ast; }
        public java.util.List<TokenInfo> getTokens() { return tokens; }
        public void setTokens(java.util.List<TokenInfo> tokens) { this.tokens = tokens; }

        public static class TokenInfo {
            private String type;
            private String value;
            private int line;
            private int column;

            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getValue() { return value; }
            public void setValue(String value) { this.value = value; }
            public int getLine() { return line; }
            public void setLine(int line) { this.line = line; }
            public int getColumn() { return column; }
            public void setColumn(int column) { this.column = column; }
        }
    }
}
