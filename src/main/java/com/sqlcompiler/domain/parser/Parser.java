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
        if (match(TokenType.REPLACE)) {
            if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                throw error(previous(), "REPLACE INTO solo es valido en MySQL/MariaDB");
            }
            return parseInsert();
        } 
        if (match(TokenType.UPDATE)) return parseUpdate();
        if (match(TokenType.DELETE)) return parseDelete();
        if (match(TokenType.CREATE)) return parseCreate();
        if (match(TokenType.ALTER)) return parseAlter();
        if (match(TokenType.GRANT)) return parseGrant();
        if (match(TokenType.MERGE)) {
            if ("mysql".equals(dialect) || "mariadb".equals(dialect)) {
                throw error(previous(), "MERGE no es soportado en " + dialect.toUpperCase());
            }
            return parseMerge();
        }
        if (match(TokenType.SHOW)) return parseShow();
        if (match(TokenType.DESCRIBE)) return parseDescribe();
        if (match(TokenType.DECLARE)) return parseDeclare();

        throw error(peek(), "Unknown SQL statement. Supported: SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, GRANT, MERGE, REPLACE, SET, SHOW, DESCRIBE, DECLARE");
    }

    private ASTNode parseSelect() {
        SelectNode select = new SelectNode();

        
        if (match(TokenType.TOP)) {
            if (!"sqlserver".equals(dialect)) {
                throw error(previous(), "TOP solo es valido en SQL Server");
            }
            select.setTop(consume(TokenType.NUMBER, "Expected number after TOP").getValue());
        }

        
        if (check(TokenType.IDENTIFIER) && peek().getValue().equalsIgnoreCase("SQL_CALC_FOUND_ROWS")) {
            if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                throw error(peek(), "SQL_CALC_FOUND_ROWS solo es valido en MySQL/MariaDB");
            }
            advance();
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
                       if ("sqlserver".equals(dialect)) {
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
                   }

                   
                   if (match(TokenType.NEXT)) {
                       if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                           throw error(previous(), "NEXT VALUE FOR solo es valido en MySQL/MariaDB");
                       }
                       consume(TokenType.VALUE, "Expected VALUE after NEXT");
                       Token forToken = consume(TokenType.IDENTIFIER, "Expected FOR after VALUE");
                       if (!forToken.getValue().equalsIgnoreCase("FOR")) {
                           throw error(forToken, "Expected FOR after VALUE, got '" + forToken.getValue() + "'");
                       }
                       String seqName = consume(TokenType.IDENTIFIER, "Expected sequence name after FOR").getValue();
                       columnExpression = "NEXT VALUE FOR " + seqName;
                       select.getColumns().add(columnExpression);
                       continue;
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
        String rightCol = parseSimpleValue();
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
            if ("sqlserver".equals(dialect)) {
                throw error(previous(), "LIMIT no es valido en SQL Server. Use TOP en su lugar");
            }
            select.setLimit(consume(TokenType.NUMBER, "Expected number after LIMIT").getValue());
        }

        
        if (match(TokenType.OFFSET)) {
            if ("sqlserver".equals(dialect)) {
                throw error(previous(), "OFFSET no es valido en SQL Server");
            }
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
    
    private String parseSimpleValue() {
        if (match(TokenType.STRING)) return previous().getValue();
        if (match(TokenType.NUMBER)) return previous().getValue();
        if (match(TokenType.ASTERISK)) return "*";
        if (match(TokenType.IDENTIFIER)) {
            String id = previous().getValue();
            if (match(TokenType.DOT)) {
                return id + "." + consume(TokenType.IDENTIFIER, "Expected column name after .").getValue();
            }
            return id;
        }
        if (match(TokenType.LBRACE)) return "{";
        if (match(TokenType.RBRACE)) return "}";
        if (match(TokenType.PIPE)) return "|";
        if (match(TokenType.CONCAT)) return "||";
        throw error(peek(), "Expected value");
    }

    private String parseColumnExpr() {
    if (match(TokenType.CASE)) {
        return parseCaseExpression();
    }

    if (match(TokenType.LPAREN)) {
        if (check(TokenType.SELECT)) {
            match(TokenType.SELECT);
            ASTNode subquery = parseSelect();
            consume(TokenType.RPAREN, "Expected )");
            return "(SELECT ...)";
        }
        throw error(previous(), "Unexpected parenthesized expression in column list");
    }

    String first = consume(TokenType.IDENTIFIER, "Expected column or function name").getValue();
    if (match(TokenType.LPAREN)) {
        validateDialectFunction(first);
        StringBuilder sb = new StringBuilder(first).append("(");
        if (match(TokenType.ASTERISK)) {
            sb.append("*");
        } else if (!check(TokenType.RPAREN)) {
            sb.append(parseSimpleValue());
            while (match(TokenType.COMMA) || check(TokenType.SEPARATOR)) {
                if (match(TokenType.SEPARATOR)) {
                    if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                        throw error(previous(), "SEPARATOR (GROUP_CONCAT) solo es valido en MySQL/MariaDB");
                    }
                    sb.append(" SEPARATOR ").append(parseSimpleValue());
                } else {
                    sb.append(", ").append(parseSimpleValue());
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
            String val = parseSimpleValue();
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
            sb.append(parseSimpleValue());
            while (match(TokenType.COMMA)) {
                sb.append(", ").append(parseSimpleValue());
            }
            right = sb.toString();
        }
        consume(TokenType.RPAREN, "Expected )");
        return new ConditionNode(col, "IN", right);
    }

    if (match(TokenType.BETWEEN)) {
        String val1 = parseSimpleValue();
        consume(TokenType.AND, "Expected AND after BETWEEN");
        String val2 = parseSimpleValue();
        return new ConditionNode(col, "BETWEEN", val1 + " AND " + val2);
    }

    String op = advance().getValue();

    if ("ILIKE".equalsIgnoreCase(op) && !"postgresql".equals(dialect)) {
        throw error(previous(), "ILIKE solo es valido en PostgreSQL");
    }

    if (match(TokenType.LPAREN)) {
        if (check(TokenType.SELECT)) {
            match(TokenType.SELECT);
            parseSelect();
            consume(TokenType.RPAREN, "Expected )");
            return new ConditionNode(col, op, "(SELECT ...)");
        }
        String val = parseSimpleValue();
        consume(TokenType.RPAREN, "Expected )");
        return new ConditionNode(col, op, "(" + val + ")");
    }

    String val = parseSimpleValue();
    return new ConditionNode(col, op, val);
}

    private ASTNode parseInsert() {
        InsertNode insert = new InsertNode();
        if (match(TokenType.IGNORE)) {
            if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                throw error(previous(), "INSERT IGNORE solo es valido en MySQL/MariaDB");
            }
        }
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
            if (!"postgresql".equals(dialect)) {
                throw error(previous(), "RETURNING solo es valido en PostgreSQL");
            }
            while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
                   && peek().getType() != TokenType.END_OF_FILE) {
                advance();
            }
        }

        
        if (match(TokenType.ON)) {
            if (check(TokenType.CONFLICT)) {
                consume(TokenType.CONFLICT, "Expected CONFLICT after ON");
                if (!"postgresql".equals(dialect)) {
                    throw error(previous(), "ON CONFLICT solo es valido en PostgreSQL");
                }
                while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
                       && peek().getType() != TokenType.END_OF_FILE) {
                    advance();
                }
            } else if (peek().getValue().equalsIgnoreCase("DUPLICATE")) {
                if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
                    throw error(peek(), "ON DUPLICATE KEY UPDATE solo es valido en MySQL/MariaDB");
                }
                while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
                       && peek().getType() != TokenType.END_OF_FILE) {
                    advance();
                }
            } else {
                throw error(peek(), "Expected CONFLICT or DUPLICATE after ON");
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
        if (match(TokenType.SEQUENCE)) {
            return parseCreateSequence();
        }
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

    private ASTNode parseCreateSequence() {
        if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
            throw error(previous(), "CREATE SEQUENCE solo es valido en MySQL/MariaDB");
        }
        StringBuilder full = new StringBuilder("CREATE SEQUENCE");
        while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
               && peek().getType() != TokenType.END_OF_FILE) {
            full.append(" ").append(advance().getValue());
        }
        final String seqStr = full.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + seqStr);
            }
            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", seqStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
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

    private ASTNode parseShow() {
        if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
            throw error(previous(), "SHOW solo es valido en MySQL/MariaDB");
        }
        StringBuilder full = new StringBuilder("SHOW");
        while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
               && peek().getType() != TokenType.END_OF_FILE) {
            full.append(" ").append(advance().getValue());
        }
        final String showStr = full.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + showStr);
            }
            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", showStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private ASTNode parseDescribe() {
        if (!"mysql".equals(dialect) && !"mariadb".equals(dialect)) {
            throw error(previous(), "DESCRIBE solo es valido en MySQL/MariaDB");
        }
        String table = consume(TokenType.IDENTIFIER, "Expected table name after DESCRIBE").getValue();
        final String descStr = "DESCRIBE " + table;
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + descStr);
            }
            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", descStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private ASTNode parseDeclare() {
        if (!"sqlserver".equals(dialect)) {
            throw error(previous(), "DECLARE solo es valido en SQL Server");
        }
        StringBuilder full = new StringBuilder("DECLARE");
        while (!isAtEnd() && peek().getType() != TokenType.SEMICOLON
               && peek().getType() != TokenType.END_OF_FILE) {
            full.append(" ").append(advance().getValue());
        }
        final String declareStr = full.toString();
        return new ASTNode() {
            @Override
            public void print(int indent) {
                System.out.println(getIndentation(indent) + declareStr);
            }
            @Override
            public Map<String, Object> toVisualTree() {
                Map<String, Object> node = new java.util.HashMap<>();
                node.put("name", declareStr);
                node.put("children", new java.util.ArrayList<>());
                return node;
            }
        };
    }

    private void validateDialectFunction(String functionName) {
        String fn = functionName.toUpperCase();

        if ("sqlserver".equals(dialect)) {
            if ("NOW".equals(fn)) {
                throw error(peek(), "NOW() no es valido en SQL Server. Use GETDATE() en su lugar");
            }
            if ("GROUP_CONCAT".equals(fn) || "STRING_AGG".equals(fn) || "ARRAY_AGG".equals(fn) ||
                "TO_CHAR".equals(fn) || "TO_DATE".equals(fn) || "DATE_FORMAT".equals(fn) ||
                "IFNULL".equals(fn) || "LAST_INSERT_ID".equals(fn) ||
                "IF".equals(fn) || "GENERATE_SERIES".equals(fn)) {
                throw error(peek(), fn + "() no es valido en SQL Server");
            }
        }

        if ("postgresql".equals(dialect)) {
            if ("GETDATE".equals(fn)) {
                throw error(peek(), "GETDATE() no es valido en PostgreSQL. Use NOW() en su lugar");
            }
            if ("GROUP_CONCAT".equals(fn)) {
                throw error(peek(), "GROUP_CONCAT() no es valido en PostgreSQL. Use STRING_AGG() en su lugar");
            }
            if ("CHARINDEX".equals(fn) || "IIF".equals(fn) || "SQUARE".equals(fn) ||
                "DATE_FORMAT".equals(fn) || "IFNULL".equals(fn) || "LAST_INSERT_ID".equals(fn) ||
                "ISNULL".equals(fn) || "IF".equals(fn) || "NEWID".equals(fn)) {
                throw error(peek(), fn + "() no es valido en PostgreSQL");
            }
        }

        if ("mysql".equals(dialect) || "mariadb".equals(dialect)) {
            if ("GETDATE".equals(fn)) {
                throw error(peek(), "GETDATE() no es valido en MySQL/MariaDB. Use NOW() en su lugar");
            }
            if ("STRING_AGG".equals(fn) || "ARRAY_AGG".equals(fn) ||
                "TO_CHAR".equals(fn) || "TO_DATE".equals(fn) ||
                "CHARINDEX".equals(fn) || "IIF".equals(fn) || "SQUARE".equals(fn) ||
                "NEWID".equals(fn)) {
                throw error(peek(), fn + "() no es valido en MySQL/MariaDB");
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