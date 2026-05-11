package com.sqlcompiler;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int position;
    private Token currentToken;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        if (!tokens.isEmpty()) {
            this.currentToken = tokens.get(0);
        }
    }

    private void advance() {
        position++;
        if (position < tokens.size()) {
            currentToken = tokens.get(position);
        }
    }

    private boolean check(TokenType type) {
        return currentToken.getType() == type;
    }

    private void expect(TokenType type) {
        if (!check(type)) {
            error("Se esperaba " + new Token(type, "", 0, 0).typeToString() +
                  " pero se encontro " + currentToken.typeToString());
        }
        advance();
    }

    private void error(String message) {
        throw new RuntimeException("Error de sintaxis en linea " + currentToken.getLine() +
                ", columna " + currentToken.getColumn() + ": " + message);
    }

    private CompOperator tokenTypeToCompOperator(TokenType type) {
        switch (type) {
            case EQUAL:         return CompOperator.EQUAL;
            case GREATER:       return CompOperator.GREATER;
            case LESS:          return CompOperator.LESS;
            case GREATER_EQUAL: return CompOperator.GREATER_EQUAL;
            case LESS_EQUAL:    return CompOperator.LESS_EQUAL;
            case NOT_EQUAL:     return CompOperator.NOT_EQUAL;
            default:
                error("Operador de comparacion invalido");
                return CompOperator.EQUAL;
        }
    }

    private ExpressionNode parseExpression() {
        if (check(TokenType.IDENTIFIER)) {
            String value = currentToken.getValue();
            advance();
            return new ExpressionNode(ExpressionNode.ExprType.IDENTIFIER, value);
        } else if (check(TokenType.NUMBER)) {
            String value = currentToken.getValue();
            advance();
            return new ExpressionNode(ExpressionNode.ExprType.NUMBER, value);
        } else if (check(TokenType.STRING)) {
            String value = currentToken.getValue();
            advance();
            return new ExpressionNode(ExpressionNode.ExprType.STRING, value);
        } else {
            error("Se esperaba una expresion (identificador, numero o string)");
            return null;
        }
    }

    private ConditionNode parseCondition() {
        ExpressionNode left = parseExpression();

        if (!check(TokenType.EQUAL) && !check(TokenType.GREATER) &&
            !check(TokenType.LESS) && !check(TokenType.GREATER_EQUAL) &&
            !check(TokenType.LESS_EQUAL) && !check(TokenType.NOT_EQUAL)) {
            error("Se esperaba un operador de comparacion (=, >, <, >=, <=, !=)");
        }

        CompOperator op = tokenTypeToCompOperator(currentToken.getType());
        advance();

        ExpressionNode right = parseExpression();

        return new ConditionNode(left, op, right);
    }

    private ConditionNode parseWhere() {
        expect(TokenType.WHERE);
        return parseCondition();
    }

    private void parseColumns(SelectNode selectNode) {
        if (check(TokenType.ASTERISK)) {
            selectNode.setSelectAll(true);
            advance();
        } else if (check(TokenType.IDENTIFIER)) {
            selectNode.getColumns().add(currentToken.getValue());
            advance();

            while (check(TokenType.COMMA)) {
                advance();
                expect(TokenType.IDENTIFIER);
                selectNode.getColumns().add(tokens.get(position - 1).getValue());
            }
        } else {
            error("Se esperaba '*' o una lista de columnas");
        }
    }

    private SelectNode parseQuery() {
        SelectNode selectNode = new SelectNode();

        expect(TokenType.SELECT);

        parseColumns(selectNode);

        expect(TokenType.FROM);

        expect(TokenType.IDENTIFIER);
        selectNode.setTableName(tokens.get(position - 1).getValue());

        if (check(TokenType.WHERE)) {
            selectNode.setWhereCondition(parseWhere());
        }

        if (check(TokenType.SEMICOLON)) {
            advance();
        }

        if (!check(TokenType.END_OF_FILE)) {
            error("Se esperaba fin de archivo despues de la consulta");
        }

        return selectNode;
    }

    public SelectNode parse() {
        return parseQuery();
    }
}