package com.sqlcompiler.application;

import java.util.ArrayList;
import java.util.List;


import com.sqlcompiler.domain.lexer.Lexer;
import com.sqlcompiler.domain.lexer.Token;
import com.sqlcompiler.domain.parser.Parser;
import com.sqlcompiler.domain.parser.ASTNode;

public class ValidateSqlUseCase {

    public ValidationResult execute(ValidationCommand command) {
        ValidationResult result = new ValidationResult();

        try {
           
  
Lexer lexer = new Lexer(command.getQuery(), command.getDialect());
            List<Token> tokens = lexer.tokenize();

            // 2. Convertir tokens a TokenInfo para la respuesta del Frontend
            List<ValidationResult.TokenInfo> tokenInfos = new ArrayList<>();
            for (Token t : tokens) {
                ValidationResult.TokenInfo info = new ValidationResult.TokenInfo();
                info.setType(t.typeToString()); // Ahora sí reconocerá tu método
                info.setValue(t.getValue());
                info.setLine(t.getLine());
                info.setColumn(t.getColumn());
                tokenInfos.add(info);
            }
            result.setTokens(tokenInfos);

            Parser parser = new Parser(tokens, null);
            ASTNode ast = parser.parse();

            
               if (ast != null) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(baos);
                java.io.PrintStream old = System.out;
                System.setOut(ps);
                ast.print(0);
                System.out.flush();
                System.setOut(old);
                result.setAst(baos.toString());
            }
            
           
            result.setValid(true);

        } catch (Exception e) {
            
            result.setValid(false);
            result.getErrors().add("Error de Sintaxis: " + e.getMessage());
        }

        return result;
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
        private List<String> errors;
        private List<String> warnings;
        private String ast;
        private List<TokenInfo> tokens;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

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
}