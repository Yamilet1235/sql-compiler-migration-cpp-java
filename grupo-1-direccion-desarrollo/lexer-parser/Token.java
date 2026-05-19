package com.sqlcompiler.domain.lexer;
public class Token{
    private TokenType type;
    private String value;
    private int line;
    private int column;

    public Token() {
        this(TokenType.INVALID, "", 0, 0);
    }
    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType(){ return type; }
    public void setType(TokenType type){ this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    public String typeToString() {
        switch (type) {
            case SELECT: return "SELECT"; case FROM: return "FROM"; case WHERE: return "WHERE";
            case INSERT: return "INSERT"; case UPDATE: return "UPDATE";
            case DELETE: return "DELETE"; case CREATE: return "CREATE";
            case INTO: return "INTO"; case VALUES: return "VALUES"; case SET: return "SET";
            case LIMIT: return "LIMIT"; case OFFSET: return "OFFSET";
            case ORDER: return "ORDER"; case GROUP: return "GROUP"; case BY: return "BY";
            case HAVING: return "HAVING"; case JOIN: return "JOIN";
            case INNER: return "INNER"; case LEFT: return "LEFT"; case RIGHT: return "RIGHT"; case ON: return "ON";
            case AND: return "AND"; case OR: return "OR";
            case ASC: return "ASC"; case DESC: return "DESC";
            case ILIKE: return "ILIKE"; case RETURNING: return "RETURNING";
            case AUTO_INCREMENT: return "AUTO_INCREMENT"; case ENGINE: return "ENGINE";
            case IDENTIFIER: return "IDENTIFIER"; case NUMBER: return "NUMBER"; case STRING: return "STRING";
            case EQUAL: return "EQUAL"; case GREATER: return "GREATER"; case LESS: return "LESS";
            case GREATER_EQUAL: return "GREATER_EQUAL"; case LESS_EQUAL: return "LESS_EQUAL";
            case NOT_EQUAL: return "NOT_EQUAL"; case ASTERISK: return "ASTERISK";
            case COMMA: return "COMMA"; case SEMICOLON: return "SEMICOLON";
            case END_OF_FILE: return "END_OF_FILE";
            default: return "UNKNOWN";
        }
    }

    @Override
    public String toString() {
        String result = "[" + typeToString() + "] ";
        if (!value.isEmpty()) {
            result += "'" + value + "' ";
        }
        result += "(L" + line + ":C" + column + ")";
        return result;
    }
}