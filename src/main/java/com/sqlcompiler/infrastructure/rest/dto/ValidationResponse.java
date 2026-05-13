package com.sqlcompiler.infrastructure.rest.dto;

import java.util.List;

public class ValidationResponse {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private String ast;
    private List<TokenInfo> tokens;

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public String getAst() { return ast; }
    public void setAst(String ast) { this.ast = ast; }

    public List<TokenInfo> getTokens() { return tokens; }
    public void setTokens(List<TokenInfo> tokens) { this.tokens = tokens; }

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
