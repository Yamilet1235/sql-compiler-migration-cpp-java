package com.sqlcompiler.domain.parser;

import java.util.List;
import java.util.ArrayList;
import com.sqlcompiler.domain.lexer.Token;
import com.sqlcompiler.domain.lexer.TokenType;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        ASTNode result = parseStatement();
        if (!isAtEnd() && peek().getType() == TokenType.SEMICOLON) {
            advance();
        }
        return result;
    }

    private ASTNode parseStatement() {
        if (match(TokenType.SELECT)) return parseSelect();
        if (match(TokenType.INSERT)) return parseInsert();
        if (match(TokenType.UPDATE)) return parseUpdate();
        if (match(TokenType.DELETE)) return parseDelete();
        if (match(TokenType.CREATE)) return parseCreate();
        throw error(peek(), "Unknown SQL statement. Supported: SELECT, INSERT, UPDATE, DELETE, CREATE");
    }

    // ===================== SELECT =====================
    private ASTNode parseSelect() {
        SelectNode select = new SelectNode();

        // TOP (SQL Server)
        if (match(TokenType.TOP)) {
            select.setTop(consume(TokenType.NUMBER, "Expected number after TOP").getValue());
        }

        // Columns
        if (match(TokenType.ASTERISK)) {
            select.setSelectAll(true);
        } else {
             do {
                select.getColumns().add(parseColumnExpr());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.FROM, "Expected FROM");

        // Table + optional alias
        String table = consume(TokenType.IDENTIFIER, "Expected table name").getValue();
        select.setTableName(table);
        if (match(TokenType.IDENTIFIER)) {
            select.setTableAlias(previous().getValue());
        } else if (match(TokenType.AS)) {
            select.setTableAlias(consume(TokenType.IDENTIFIER, "Expected alias").getValue());
        }

        // JOINs
        while (match(TokenType.JOIN) || match(TokenType.INNER) || match(TokenType.LEFT) || match(TokenType.RIGHT)) {
            String joinType = "JOIN";
            if (previous().getType() == TokenType.INNER) {
                joinType = "INNER";
                consume(TokenType.JOIN, "Expected JOIN");
            } else if (previous().getType() == TokenType.LEFT) {
                joinType = match(TokenType.JOIN) ? "LEFT" : "LEFT OUTER";
            } else if (previous().getType() == TokenType.RIGHT) {
                joinType = match(TokenType.JOIN) ? "RIGHT" : "RIGHT OUTER";
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

        // WHERE
        if (match(TokenType.WHERE)) {
            select.setWhereCondition(parseOrCondition());
        }
                // GROUP BY
        if (match(TokenType.GROUP)) {
            consume(TokenType.BY, "Expected BY after GROUP");
            do { parseSimpleColumnRef(); } while (match(TokenType.COMMA));
            
            // HAVING
            if (match(TokenType.HAVING)) {
                while (!isAtEnd() && peek().getType() != TokenType.ORDER 
                       && peek().getType() != TokenType.LIMIT && peek().getType() != TokenType.OFFSET 
                       && peek().getType() != TokenType.SEMICOLON && peek().getType() != TokenType.END_OF_FILE) {
                    advance();
                }
            }
        }
        // ORDER BY
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

        // LIMIT (MySQL/PostgreSQL)
        if (match(TokenType.LIMIT)) {
            select.setLimit(consume(TokenType.NUMBER, "Expected number after LIMIT").getValue());
        }

        // OFFSET
        if (match(TokenType.OFFSET)) {
            select.setOffset(consume(TokenType.NUMBER, "Expected number after OFFSET").getValue());
        }

        return select;
    }

          // Para JOIN, ORDER BY, INSERT (solo identificador con punto)
    private String parseSimpleColumnRef() {
        String first = consume(TokenType.IDENTIFIER, "Expected column name").getValue();
        if (match(TokenType.DOT)) {
            return first + "." + consume(TokenType.IDENTIFIER, "Expected column name after .").getValue();
        }
        return first;
    }
    
    // Para SELECT (incluye funciones como MAX, COUNT, etc.)
    private String parseColumnExpr() {
        String first = consume(TokenType.IDENTIFIER, "Expected column or function name").getValue();
        if (match(TokenType.LPAREN)) {
            StringBuilder sb = new StringBuilder(first).append("(");
            if (match(TokenType.ASTERISK)) {
                sb.append("*");
            } else {
                sb.append(consumeAnyValue().getValue());
                while (match(TokenType.COMMA)) {
                    sb.append(", ").append(consumeAnyValue().getValue());
                }
            }
            consume(TokenType.RPAREN, "Expected )");
            sb.append(")");
            return sb.toString();
        }
        if (match(TokenType.DOT)) {
            return first + "." + consume(TokenType.IDENTIFIER, "Expected column name after .").getValue();
        }
        return first;
    }
    // ===================== WHERE condition parsing =====================
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
        // Parenthesized expression
        if (match(TokenType.LPAREN)) {
            ConditionNode inner = parseOrCondition();
            consume(TokenType.RPAREN, "Expected )");
            return inner;
        }
        // Simple: column op value
        String col = parseSimpleColumnRef();
        String op = advance().getValue();
        String val = consumeAnyValue().getValue();
        return new ConditionNode(col, op, val);
    }

    // ===================== INSERT =====================
    private ASTNode parseInsert() {
        InsertNode insert = new InsertNode();
        if (match(TokenType.INTO)) { }
        insert.table = consume(TokenType.IDENTIFIER, "Expected table name").getValue();

        // Optional column list: (col1, col2, ...)
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
        return insert;
    }

    // ===================== UPDATE =====================
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

    // ===================== DELETE =====================
    private ASTNode parseDelete() {
        DeleteNode delete = new DeleteNode();
        if (match(TokenType.FROM)) { }
        delete.table = consume(TokenType.IDENTIFIER, "Expected table").getValue();
        if (match(TokenType.WHERE)) {
            delete.condition = parseOrCondition();
        }
        return delete;
    }

    // ===================== CREATE TABLE =====================
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

    // ===================== Utils =====================
    private Token consumeAnyValue() {
        if (match(TokenType.STRING)) return previous();
        if (match(TokenType.NUMBER)) return previous();
        if (match(TokenType.IDENTIFIER)) return previous();
        if (match(TokenType.ASTERISK)) return previous();
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
}

