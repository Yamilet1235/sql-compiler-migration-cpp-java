package com.sqlcompiler.domain.parser;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

import com.sqlcompiler.domain.lexer.Token;
import com.sqlcompiler.domain.lexer.TokenType;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;
    private final String dialect; 

    public Parser(List<Token> tokens, String dialect) {
        this.tokens = tokens;
        this.dialect = dialect != null ? dialect.toLowerCase() : "";
    }

    public ASTNode parse() {
        ASTNode result = parseStatement();
        if (!isAtEnd()) {
            if (peek().getType() == TokenType.SEMICOLON) {
                advance();
            }
        }
        if (!isAtEnd()) {
            throw error(peek(), "Sintaxis invalida: caracter(es) no esperados '" + peek().getValue() + "' despues del final de la sentencia");
        }
        return result;
    }

    private ASTNode parseStatement() {
        boolean isMongoQuery = !tokens.isEmpty() && tokens.get(0).getValue().equalsIgnoreCase("db");

        
        if ("mongodb".equals(dialect)) {
            if (!tokens.isEmpty() && (
                check(TokenType.SELECT) || check(TokenType.INSERT) || 
                check(TokenType.UPDATE) || check(TokenType.DELETE) || 
                check(TokenType.CREATE))) {
                throw error(peek(), "El dialecto seleccionado es MongoDB, pero la consulta es SQL. Use sintaxis nativa de MongoDB o cambie el dialecto.");
            }
            
            if (isMongoQuery) {
                for (int i = 0; i < tokens.size() - 1; i++) {
                    if (tokens.get(i).getType() == TokenType.DOT &&
                        tokens.get(i + 1).getType() == TokenType.DOT) {
                        throw error(tokens.get(i), "Sintaxis MongoDB invalida: doble punto (..) detectado");
                    }
                }
                validateMongoQuery();
                int semiIdx = -1;
                for (int i = 0; i < tokens.size(); i++) {
                    if (tokens.get(i).getType() == TokenType.SEMICOLON) {
                        semiIdx = i;
                        break;
                    }
                }
                if (semiIdx >= 0) {
                    current = semiIdx;
                } else {
                    current = tokens.size() - 1;
                }
                final String mongoQuery = sourceFromTokens();
                return new ASTNode() {
                    @Override
                    public void print(int indent) {
                        System.out.println(getIndentation(indent) + "MongoDB Query:");
                        System.out.println(getIndentation(indent + 2) + mongoQuery);
                    }

                    @Override
                    public Map<String, Object> toVisualTree() {
                        Map<String, Object> node = new java.util.HashMap<>();
                        node.put("name", "MongoDB Query");
                        java.util.List<Map<String, Object>> children = new java.util.ArrayList<>();
                        Map<String, Object> queryNode = new java.util.HashMap<>();
                        queryNode.put("name", mongoQuery);
                        children.add(queryNode);
                        node.put("children", children);
                        return node;
                    }
                };
            }
        } else {
            if (isMongoQuery) {
                throw error(peek(), "El dialecto seleccionado es SQL, pero la consulta utiliza sintaxis de MongoDB (db.). Escriba una sentencia SQL válida o cambie el dialecto.");
            }
        }
       
        if (match(TokenType.SELECT)) return parseSelect();
        if (match(TokenType.SET)) return parseSetStatement();
        if (match(TokenType.INSERT)) return parseInsert();
        if (match(TokenType.REPLACE)) return parseInsert(); 
        if (match(TokenType.UPDATE)) return parseUpdate();
        if (match(TokenType.DELETE)) return parseDelete();
        if (match(TokenType.CREATE)) return parseCreate();
        if (match(TokenType.ALTER)) return parseAlter();
        if (match(TokenType.GRANT)) return parseGrant();
        if (match(TokenType.MERGE)) return parseMerge();

        throw error(peek(), "Unknown SQL statement. Supported: SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, GRANT, MERGE, REPLACE, SET");
    }

    private ASTNode parseSelect() {
        SelectNode select = new SelectNode();

        
        if (match(TokenType.TOP)) {
            select.setTop(consume(TokenType.NUMBER, "Expected number after TOP").getValue());
        }

       
        if (match(TokenType.ASTERISK)) {
            select.setSelectAll(true);
        } else {
             do {
                  
                  String columnExpression;

                  
                  if (check(TokenType.IDENTIFIER) && peek().getValue().startsWith("@")) {
                      if (!"sqlserver".equals(dialect) && !"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                          throw error(peek(), "Variables de usuario (@) no estan soportadas en " + dialect.toUpperCase());
                      }
                      int save = current;
                      Token varToken = advance();
                      if (match(TokenType.EQUAL)) {
                          
                          columnExpression = parseColumnExpr();
                          if (match(TokenType.AS)) {
                              String alias = consume(TokenType.IDENTIFIER, "Expected alias after AS").getValue();
                              columnExpression += " AS " + alias;
                          }
                          select.getColumns().add(columnExpression);
                          continue;
                      } else {
                          current = save;
                      }
                  }

                  columnExpression = parseColumnExpr();
                  
                 
                  if (match(TokenType.AS)) {
                      
                      String alias = consume(TokenType.IDENTIFIER, "Expected alias after AS").getValue();
                      columnExpression += " AS " + alias;
                  } 
                  else if (peek().getType() == TokenType.IDENTIFIER && peek().getValue().equalsIgnoreCase("AS")) {
                     
                      advance(); 
                      String alias = consume(TokenType.IDENTIFIER, "Expected alias after AS").getValue();
                      columnExpression += " AS " + alias;
                  }
                  else if (peek().getType() == TokenType.IDENTIFIER) {
                      
                      
                      if (!peek().getValue().equalsIgnoreCase("FROM")) {
                          String alias = advance().getValue();
                          columnExpression += " " + alias;
                      }
                  }
                  
                  select.getColumns().add(columnExpression);
             } while (match(TokenType.COMMA));
        }

        
        if (match(TokenType.FROM)) {
           
            String table = consume(TokenType.IDENTIFIER, "Expected table name").getValue();
            select.setTableName(table);
            if (match(TokenType.IDENTIFIER)) {
                select.setTableAlias(previous().getValue());
            } else if (match(TokenType.AS)) {
                select.setTableAlias(consume(TokenType.IDENTIFIER, "Expected alias").getValue());
            }
        }

       
        while (match(TokenType.JOIN) || match(TokenType.INNER) || match(TokenType.LEFT) || match(TokenType.RIGHT) || match(TokenType.FULL)) {
    String joinType = "JOIN";
    if (previous().getType() == TokenType.INNER) {
        joinType = "INNER";
        consume(TokenType.JOIN, "Expected JOIN");
    } else if (previous().getType() == TokenType.LEFT) {
        joinType = match(TokenType.JOIN) ? "LEFT" : "LEFT OUTER";
    } else if (previous().getType() == TokenType.RIGHT) {
        joinType = match(TokenType.JOIN) ? "RIGHT" : "RIGHT OUTER";
    } else if (previous().getType() == TokenType.FULL) {
        joinType = match(TokenType.OUTER) ? "FULL OUTER" : "FULL";
        consume(TokenType.JOIN, "Expected JOIN after FULL");
    }

        String joinTable = consume(TokenType.IDENTIFIER, "Expected table name after JOIN").getValue();
        String joinAlias = null;
        if (match(TokenType.IDENTIFIER)) {
          joinAlias = previous().getValue();
        }
        consume(TokenType.ON, "Expected ON after JOIN");

        String leftCol = parseSimpleColumnRef();
        String op = advance().getValue();
        String rightCol = parseSimpleColumnRef();
        ConditionNode onCond = new ConditionNode(leftCol, op, rightCol);

         select.getJoins().add(new SelectNode.JoinInfo(joinType, joinTable, joinAlias, onCond));
    }

      
        if (match(TokenType.WHERE)) {
            select.setWhereCondition(parseOrCondition());
        }
        
        
        if (match(TokenType.GROUP)) {
            consume(TokenType.BY, "Expected BY after GROUP");
            do { parseSimpleColumnRef(); } while (match(TokenType.COMMA));
            
      
            if (match(TokenType.HAVING)) {
                while (!isAtEnd() && peek().getType() != TokenType.ORDER 
                       && peek().getType() != TokenType.LIMIT && peek().getType() != TokenType.OFFSET 
                       && peek().getType() != TokenType.SEMICOLON && peek().getType() != TokenType.END_OF_FILE) {
                    advance();
                }
            }
        }
        
      
        if (match(TokenType.ORDER)) {
            consume(TokenType.BY, "Expected BY after ORDER");
            do {
                String col = parseSimpleColumnRef();
                boolean asc = true;
                if (match(TokenType.ASC)) { asc = true; }
                else if (match(TokenType.DESC)) { asc = false; }
                select.getOrderByColumns().add(col);
                select.getOrderByAsc().add(asc);
            } while (match(TokenType.COMMA));
        }

        
        if (match(TokenType.LIMIT)) {
            select.setLimit(consume(TokenType.NUMBER, "Expected number after LIMIT").getValue());
        }

       
        if (match(TokenType.OFFSET)) {
            select.setOffset(consume(TokenType.NUMBER, "Expected number after OFFSET").getValue());
        }

        return select;
    }

    private String parseSimpleColumnRef() {
        String first = consume(TokenType.IDENTIFIER, "Expected column name").getValue();
        if (match(TokenType.DOT)) {
            return first + "." + consume(TokenType.IDENTIFIER, "Expected column name after .").getValue();
        }
        return first;
    }
    
    private String parseColumnExpr() {
    if (match(TokenType.CASE)) {
        return parseCaseExpression();
    }
    String first = consume(TokenType.IDENTIFIER, "Expected column or function name").getValue();
    if (match(TokenType.LPAREN)) {
        validateDialectFunction(first);
        StringBuilder sb = new StringBuilder(first).append("(");
        if (match(TokenType.ASTERISK)) {
            sb.append("*");
        } else if (!check(TokenType.RPAREN)) {
            sb.append(consumeAnyValue().getValue());
            while (match(TokenType.COMMA) || check(TokenType.SEPARATOR)) {
                if (match(TokenType.SEPARATOR)) {
                    sb.append(" SEPARATOR ").append(consumeAnyValue().getValue());
                } else {
                    sb.append(", ").append(consumeAnyValue().getValue());
                }
            }
        }
        consume(TokenType.RPAREN, "Expected )");
        sb.append(")");
        return sb.toString();
    }
    if (match(TokenType.DOT)) {
        return first + "." + consume(TokenType.IDENTIFIER, "Expected column name after .").getValue();
    }
    while (match(TokenType.CONCAT)) {
        first += " ||";
        if (match(TokenType.STRING) || match(TokenType.NUMBER) || match(TokenType.IDENTIFIER)) {
            first += " " + previous().getValue();
        } else {
            break;
        }
    }
    return first;
}
        private String parseCaseExpression() {
    StringBuilder sb = new StringBuilder("CASE");

    if (!check(TokenType.WHEN)) {
        sb.append(" ").append(consumeAnyValue().getValue());
    }

    while (match(TokenType.WHEN)) {
        sb.append(" WHEN");
        int depth = 0;
        while (!isAtEnd()) {
            if (check(TokenType.THEN) && depth == 0) break;
            if (check(TokenType.LPAREN)) depth++;
            if (check(TokenType.RPAREN)) depth--;
            sb.append(" ").append(advance().getValue());
        }
        consume(TokenType.THEN, "Expected THEN");
        sb.append(" THEN");
        while (!isAtEnd()) {
            if (check(TokenType.WHEN) || check(TokenType.ELSE) || check(TokenType.END)) break;
            sb.append(" ").append(advance().getValue());
        }
    }

    if (match(TokenType.ELSE)) {
        sb.append(" ELSE");
        while (!check(TokenType.END) && !isAtEnd()) {
            sb.append(" ").append(advance().getValue());
        }
    }

    consume(TokenType.END, "Expected END");
    sb.append(" END");
    return sb.toString();
}

   
    private ConditionNode parseOrCondition() {
        ConditionNode left = parseAndCondition();
        while (match(TokenType.OR)) {
            ConditionNode right = parseAndCondition();
            left = new ConditionNode(ConditionNode.CondType.OR, left, right);
        }
        return left;
    }

    private ConditionNode parseAndCondition() {
        ConditionNode left = parseNotCondition();
        while (match(TokenType.AND)) {
            ConditionNode right = parseNotCondition();
            left = new ConditionNode(ConditionNode.CondType.AND, left, right);
        }
        return left;
    }

    private ConditionNode parseNotCondition() {
        if (match(TokenType.NOT)) {
            return new ConditionNode(ConditionNode.CondType.NOT, parsePrimaryCondition());
        }
        return parsePrimaryCondition();
    }

    private ConditionNode parsePrimaryCondition() {
    if (match(TokenType.LPAREN)) {
        if (check(TokenType.SELECT)) {
            match(TokenType.SELECT);
            ASTNode subquery = parseSelect();
            consume(TokenType.RPAREN, "Expected )");
            String op = advance().getValue();
            String val = consumeAnyValue().getValue();
            return new ConditionNode("(subquery)", op, val);
        }
        ConditionNode inner = parseOrCondition();
        consume(TokenType.RPAREN, "Expected )");
        return inner;
    }

    String col = parseSimpleColumnRef();

    if (match(TokenType.IN)) {
        consume(TokenType.LPAREN, "Expected ( after IN");
        String right;
        if (check(TokenType.SELECT)) {
            match(TokenType.SELECT);
            parseSelect();
            right = "(SELECT ...)";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(consumeAnyValue().getValue());
            while (match(TokenType.COMMA)) {
                sb.append(", ").append(consumeAnyValue().getValue());
            }
            right = sb.toString();
        }
        consume(TokenType.RPAREN, "Expected )");
        return new ConditionNode(col, "IN", right);
    }

    if (match(TokenType.BETWEEN)) {
        String val1 = consumeAnyValue().getValue();
        consume(TokenType.AND, "Expected AND after BETWEEN");
        String val2 = consumeAnyValue().getValue();
        return new ConditionNode(col, "BETWEEN", val1 + " AND " + val2);
    }

    String op = advance().getValue();

    if (match(TokenType.LPAREN)) {
        if (check(TokenType.SELECT)) {
            match(TokenType.SELECT);
            parseSelect();
            consume(TokenType.RPAREN, "Expected )");
            return new ConditionNode(col, op, "(SELECT ...)");
        }
        String val = consumeAnyValue().getValue();
        consume(TokenType.RPAREN, "Expected )");
        return new ConditionNode(col, op, "(" + val + ")");
    }

    String val = consumeAnyValue().getValue();
    return new ConditionNode(col, op, val);
}

    private ASTNode parseInsert() {
        InsertNode insert = new InsertNode();
        match(TokenType.IGNORE);
        if (match(TokenType.INTO)) { }
        insert.table = consume(TokenType.IDENTIFIER, "Expected table name").getValue();

        if (match(TokenType.LPAREN)) {
            insert.columns.add(parseSimpleColumnRef());
            while (match(TokenType.COMMA)) {
                insert.columns.add(parseSimpleColumnRef());
            }
            consume(TokenType.RPAREN, "Expected )");
        }

        if (match(TokenType.VALUES)) { }
        if (match(TokenType.LPAREN)) {
            do {
                insert.values.add(consumeAnyValue().getValue());
            } while (match(TokenType.COMMA));
            consume(TokenType.RPAREN, "Expected )");
        } else {
            insert.values.add(consumeAnyValue().getValue());
        }

        
        if (match(TokenType.RETURNING)) {
            while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
                   && peek().getType() != TokenType.END_OF_FILE) {
                advance();
            }
        }

        
        if (match(TokenType.ON)) {
            consume(TokenType.CONFLICT, "Expected CONFLICT after ON");
            while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
                   && peek().getType() != TokenType.END_OF_FILE) {
                advance();
            }
        }

        return insert;
    }


    private ASTNode parseUpdate() {
        UpdateNode update = new UpdateNode();
        update.table = consume(TokenType.IDENTIFIER, "Expected table").getValue();
        consume(TokenType.SET, "Expected SET");
        do {
            String column = parseSimpleColumnRef();
            consume(TokenType.EQUAL, "Expected =");
            String value = consumeAnyValue().getValue();
            update.setColumns.put(column, value);
        } while (match(TokenType.COMMA));
        if (match(TokenType.WHERE)) {
            update.condition = parseOrCondition();
        }
        return update;
    }

    
    private ASTNode parseDelete() {
        DeleteNode delete = new DeleteNode();
        if (match(TokenType.FROM)) { }
        delete.table = consume(TokenType.IDENTIFIER, "Expected table").getValue();
        if (match(TokenType.WHERE)) {
            delete.condition = parseOrCondition();
        }
        return delete;
    }

    
    private ASTNode parseCreate() {
        consume(TokenType.TABLE, "Expected TABLE after CREATE");
        CreateTableNode create = new CreateTableNode();
        create.table = consume(TokenType.IDENTIFIER, "Expected table name").getValue();
        consume(TokenType.LPAREN, "Expected (");
        do {
            String column = consume(TokenType.IDENTIFIER, "Expected column").getValue();
            String type = consume(TokenType.IDENTIFIER, "Expected type").getValue();
            create.columns.put(column, type);
        } while (match(TokenType.COMMA));
        consume(TokenType.RPAREN, "Expected )");
        return create;
    }

    
    private ASTNode parseAlter() {
        consume(TokenType.TABLE, "Expected TABLE after ALTER");
        String table = consume(TokenType.IDENTIFIER, "Expected table name").getValue();
        StringBuilder rest = new StringBuilder();
        while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
               && peek().getType() != TokenType.END_OF_FILE) {
            rest.append(" ").append(advance().getValue());
        }
        final String t = table;
        final String restStr = rest.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + "ALTER TABLE: " + t + restStr);
            }

            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", "ALTER TABLE: " + t + restStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private ASTNode parseGrant() {
        StringBuilder full = new StringBuilder("GRANT");
        while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
               && peek().getType() != TokenType.END_OF_FILE) {
            full.append(" ").append(advance().getValue());
        }
        final String grantStr = full.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + grantStr);
            }

            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", grantStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private ASTNode parseMerge() {
        StringBuilder full = new StringBuilder("MERGE");
        while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
               && peek().getType() != TokenType.END_OF_FILE) {
            full.append(" ").append(advance().getValue());
        }
        final String mergeStr = full.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + mergeStr);
            }

            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", mergeStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private ASTNode parseSetStatement() {
        StringBuilder full = new StringBuilder();
        full.append(consumeAnyValue().getValue());
        if (match(TokenType.EQUAL)) {
            full.append(" =");
            if (match(TokenType.LPAREN)) {
                full.append(" (");
                int depth = 1;
                while (depth > 0 && !isAtEnd()) {
                    if (check(TokenType.LPAREN)) depth++;
                    if (check(TokenType.RPAREN)) depth--;
                    if (depth == 0) break;
                    full.append(" ").append(advance().getValue());
                }
                if (match(TokenType.RPAREN)) {
                    full.append(" )");
                }
            } else {
                full.append(" ").append(consumeAnyValue().getValue());
            }
        }
        final String setStr = full.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + "SET: " + setStr);
            }

            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", "SET: " + setStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private Token consumeAnyValue() {
        if (match(TokenType.STRING)) return previous();
        if (match(TokenType.NUMBER)) return previous();
        if (match(TokenType.IDENTIFIER)) return previous();
        if (match(TokenType.ASTERISK)) return previous();
        if (match(TokenType.LBRACE)) return previous();
        if (match(TokenType.RBRACE)) return previous();
        if (match(TokenType.PIPE)) return previous();
        if (match(TokenType.CONCAT)) return previous();
        throw error(peek(), "Expected value");
    }

    private boolean match(TokenType type) {
        if (check(type)) { advance(); return true; }
        return false;
    }

    private Token consume(TokenType type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.END_OF_FILE;
    }

    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException("Error at '" + token.getValue() + "': " + message);
    }

    private void validateMongoQuery() {
        Set<String> validMethods = Set.of(
            "find", "findOne", "findOneAndUpdate", "findOneAndDelete", "findOneAndReplace",
            "insertOne", "insertMany",
            "updateOne", "updateMany",
            "deleteOne", "deleteMany",
            "replaceOne",
            "aggregate",
            "countDocuments", "estimatedDocumentCount",
            "distinct",
            "drop",
            "createIndex", "dropIndex", "createIndexes", "dropIndexes",
            "bulkWrite",
            "renameCollection",
            "createCollection", "getCollection",
            "watch",
            "mapReduce",
            "findAndModify",
            "sort", "limit", "skip", "project", "pretty", "collation"
        );

        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType() == TokenType.END_OF_FILE) break;

            if (t.getType() == TokenType.STRING) {
                inString = !inString;
                if (inString) stringChar = t.getValue().charAt(0);
                continue;
            }

            if (t.getType() == TokenType.LBRACE) braceCount++;
            if (t.getType() == TokenType.RBRACE) braceCount--;
            if (t.getType() == TokenType.LBRACKET) bracketCount++;
            if (t.getType() == TokenType.RBRACKET) bracketCount--;

            if (i > 0 && tokens.get(i - 1).getType() == TokenType.DOT &&
                t.getType() == TokenType.IDENTIFIER &&
                i + 1 < tokens.size() && tokens.get(i + 1).getType() == TokenType.LPAREN) {
                String methodName = t.getValue();
                if (!validMethods.contains(methodName)) {
                    throw error(t, "Metodo MongoDB '" + methodName + "' no es valido. Metodos validos: insertOne, find, updateMany, aggregate, etc.");
                }
            }

            if (t.getType() == TokenType.IDENTIFIER && "ObjectId".equals(t.getValue()) &&
                i + 3 < tokens.size() &&
                tokens.get(i + 1).getType() == TokenType.LPAREN &&
                tokens.get(i + 2).getType() == TokenType.STRING &&
                tokens.get(i + 3).getType() == TokenType.RPAREN) {
                String hex = tokens.get(i + 2).getValue();
                if (hex.length() != 24 || !hex.matches("[0-9a-fA-F]+")) {
                    throw error(tokens.get(i + 2), "ObjectId invalido: '" + hex + "' debe tener exactamente 24 caracteres hexadecimales");
                }
            }
        }

        if (braceCount != 0) {
            throw error(tokens.get(0), "Estructura BSON invalida: llaves {} no estan balanceadas");
        }
        if (bracketCount != 0) {
            throw error(tokens.get(0), "Estructura JSON invalida: corchetes [] no estan balanceados");
        }
    }

    private void validateDialectFunction(String functionName) {
        String fn = functionName.toUpperCase();
        if ("sqlserver".equals(dialect)) {
            if ("NOW".equals(fn)) {
                throw error(peek(), "NOW() no es valido en SQL Server. Use GETDATE() en su lugar");
            }
        }
        if ("postgresql".equals(dialect)) {
            if ("GETDATE".equals(fn)) {
                throw error(peek(), "GETDATE() no es valido en PostgreSQL. Use NOW() en su lugar");
            }
        }
    }

    private String sourceFromTokens() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.getType() == TokenType.END_OF_FILE) break;
            if (i > 0) sb.append(" ");
            sb.append(t.getValue());
        }
        return sb.toString();
    }
}