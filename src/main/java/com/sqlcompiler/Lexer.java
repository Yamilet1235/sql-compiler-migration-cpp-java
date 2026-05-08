package com.sqlcompiler;

import java.util.List;
import java.util.ArrayList;

public class Lexer {
    private final String source;
    private int position;
    private int line;
    private int column;
    private char currentChar;

    public Lexer(String src) {
        this.source = src;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.currentChar = source.isEmpty() ? '\0' : source.charAt(0);
    }

    private void advance() {
        if (currentChar == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        position++;
        if (position >= source.length()) {
            currentChar = '\0';
        } else {
            currentChar = source.charAt(position);
        }
    }

    private char peek() {
        int nextPos = position + 1;
        if (nextPos >= source.length()) {
            return '\0';
        }
        return source.charAt(nextPos);
    }

    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }

        if (currentChar == '-' && peek() == '-') {
            while (currentChar != '\0' && currentChar != '\n') {
                advance();
            }
            if (currentChar == '\n') {
                advance();
            }
            skipWhitespace();
        }
    }

    private boolean isKeyword(String str) {
        String upper = str.toUpperCase();
        return upper.equals("SELECT") || upper.equals("FROM") || upper.equals("WHERE");
    }

    private TokenType keywordToTokenType(String str) {
        String upper = str.toUpperCase();
        switch (upper) {
            case "SELECT": return TokenType.SELECT;
            case "FROM":   return TokenType.FROM;
            case "WHERE":  return TokenType.WHERE;
            default:       return TokenType.IDENTIFIER;
        }
    }

    private Token readIdentifierOrKeyword() {
        int startLine = line;
        int startCol = column;
        StringBuilder value = new StringBuilder();

        while (currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
            value.append(currentChar);
            advance();
        }

        if (isKeyword(value.toString())) {
            return new Token(keywordToTokenType(value.toString()), value.toString(), startLine, startCol);
        }

        return new Token(TokenType.IDENTIFIER, value.toString(), startLine, startCol);
    }

    private Token readNumber() {
        int startLine = line;
        int startCol = column;
        StringBuilder value = new StringBuilder();

        while (currentChar != '\0' && Character.isDigit(currentChar)) {
            value.append(currentChar);
            advance();
        }

        return new Token(TokenType.NUMBER, value.toString(), startLine, startCol);
    }

    private Token readString() {
        int startLine = line;
        int startCol = column;
        StringBuilder value = new StringBuilder();

        advance();

        while (currentChar != '\0' && currentChar != '\'') {
            value.append(currentChar);
            advance();
        }

        if (currentChar == '\'') {
            advance();
        } else {
            return new Token(TokenType.INVALID, value.toString(), startLine, startCol);
        }

        return new Token(TokenType.STRING, value.toString(), startLine, startCol);
    }

    public Token getNextToken() {
        skipWhitespace();

        if (currentChar == '\0') {
            return new Token(TokenType.END_OF_FILE, "", line, column);
        }

        int startLine = line;
        int startCol = column;

        if (Character.isLetter(currentChar) || currentChar == '_') {
            return readIdentifierOrKeyword();
        }

        if (Character.isDigit(currentChar)) {
            return readNumber();
        }

        if (currentChar == '\'') {
            return readString();
        }

        if (currentChar == '>') {
            advance();
            if (currentChar == '=') {
                advance();
                return new Token(TokenType.GREATER_EQUAL, ">=", startLine, startCol);
            }
            return new Token(TokenType.GREATER, ">", startLine, startCol);
        }

        if (currentChar == '<') {
            advance();
            if (currentChar == '=') {
                advance();
                return new Token(TokenType.LESS_EQUAL, "<=", startLine, startCol);
            }
            return new Token(TokenType.LESS, "<", startLine, startCol);
        }

        if (currentChar == '!') {
            advance();
            if (currentChar == '=') {
                advance();
                return new Token(TokenType.NOT_EQUAL, "!=", startLine, startCol);
            }
            return new Token(TokenType.INVALID, "!", startLine, startCol);
        }

        char ch = currentChar;
        advance();

        switch (ch) {
            case '=': return new Token(TokenType.EQUAL, "=", startLine, startCol);
            case '*': return new Token(TokenType.ASTERISK, "*", startLine, startCol);
            case ',': return new Token(TokenType.COMMA, ",", startLine, startCol);
            case ';': return new Token(TokenType.SEMICOLON, ";", startLine, startCol);
            default:  return new Token(TokenType.INVALID, String.valueOf(ch), startLine, startCol);
        }
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token token;

        do {
            token = getNextToken();
            tokens.add(token);
        } while (token.getType() != TokenType.END_OF_FILE);

        return tokens;
    }
}