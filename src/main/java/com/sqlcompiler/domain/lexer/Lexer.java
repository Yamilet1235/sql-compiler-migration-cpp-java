package com.sqlcompiler.domain.lexer;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class Lexer {
    private final String source;
    private int position;
    private int line;
    private int column;
    private char currentChar;
    private final String dialect;

    public Lexer(String src, String dialect ){
        this.source = src;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.currentChar = source.isEmpty() ? '\0' : source.charAt(0);
        this.dialect= dialect;
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
        return;
    }

   
    if (currentChar == '/' && peek() == '*') {
        advance(); 
        advance(); 
        while (currentChar != '\0') {
            if (currentChar == '*' && peek() == '/') {
                advance(); 
                advance(); 
                break;
            }
            advance();
        }
        skipWhitespace();
    }
}

       private static final Set<String> KEYWORDS = Set.of(
    "SELECT","FROM","WHERE",
    "INSERT","UPDATE","DELETE","CREATE",
    "INTO","VALUES","SET",
    "LIMIT","OFFSET","TOP",
    "ORDER","GROUP","BY","HAVING",
    "JOIN","INNER","LEFT","RIGHT","FULL","OUTER","ON",
    "AND","OR","NOT","AS","IN","BETWEEN","LIKE",
    "CASE","WHEN","THEN","ELSE","END",
    "ASC","DESC",
    "ILIKE","RETURNING",
    "AUTO_INCREMENT","ENGINE",
    "TABLE",
    "SEPARATOR","IGNORE","REPLACE","ALTER","GRANT","MERGE","USING","MATCHED",
    "CONFLICT","DO","EXCLUDED"
);

    private boolean isKeyword(String str) {
        return KEYWORDS.contains(str.toUpperCase());
    }

    private TokenType keywordToTokenType(String str) {
        switch (str.toUpperCase()) {
         case "SELECT": return TokenType.SELECT;
         case "FROM": return TokenType.FROM;
         case "WHERE": return TokenType.WHERE;
         case "INSERT": return TokenType.INSERT;
         case "UPDATE": return TokenType.UPDATE;
         case "DELETE": return TokenType.DELETE;
         case "CREATE": return TokenType.CREATE;
         case "INTO": return TokenType.INTO;
         case "VALUES": return TokenType.VALUES;
         case "SET": return TokenType.SET;
         case "LIMIT": return TokenType.LIMIT;
         case "OFFSET": return TokenType.OFFSET;
         case "ORDER": return TokenType.ORDER;
         case "GROUP": return TokenType.GROUP;
         case "BY": return TokenType.BY;
         case "HAVING": return TokenType.HAVING;
         case "JOIN": return TokenType.JOIN;
         case "INNER": return TokenType.INNER;
         case "LEFT": return TokenType.LEFT;
         case "RIGHT": return TokenType.RIGHT;
         case "ON": return TokenType.ON;
         case "AND": return TokenType.AND;
         case "OR": return TokenType.OR;
         case "ASC": return TokenType.ASC;
         case "DESC": return TokenType.DESC;
         case "ILIKE": return TokenType.ILIKE;
         case "RETURNING": return TokenType.RETURNING;
         case "AUTO_INCREMENT": return TokenType.AUTO_INCREMENT;
         case "ENGINE": return TokenType.ENGINE;
         case "TABLE": return TokenType.TABLE;        
         case "TOP": return TokenType.TOP;
         case "NOT": return TokenType.NOT;
         case "AS": return TokenType.AS;
         case "FULL": return TokenType.FULL;
         case "OUTER": return TokenType.OUTER;
         case "IN": return TokenType.IN;
         case "BETWEEN": return TokenType.BETWEEN;
         case "LIKE": return TokenType.LIKE;
         case "CASE": return TokenType.CASE;
         case "WHEN": return TokenType.WHEN;
         case "THEN": return TokenType.THEN;
         case "ELSE": return TokenType.ELSE;
         case "END": return TokenType.END;
         case "SEPARATOR": return TokenType.SEPARATOR;
         case "IGNORE": return TokenType.IGNORE;
         case "REPLACE": return TokenType.REPLACE;
         case "ALTER": return TokenType.ALTER;
         case "GRANT": return TokenType.GRANT;
         case "MERGE": return TokenType.MERGE;
         case "USING": return TokenType.USING;
         case "MATCHED": return TokenType.MATCHED;
         case "CONFLICT": return TokenType.CONFLICT;
         case "DO": return TokenType.DO;
         case "EXCLUDED": return TokenType.EXCLUDED;
         default: return TokenType.IDENTIFIER;

     }
    }

    private Token readIdentifierOrKeyword() {
        int startLine = line;
        int startCol = column;
        StringBuilder value = new StringBuilder();

        while (currentChar != '\0' && (Character.isLetterOrDigit(currentChar) || currentChar == '_' || currentChar == '@')) {
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

        if (currentChar == '.' && peek() != '\0' && Character.isDigit(peek())) {
            value.append('.');
            advance();
            while (currentChar != '\0' && Character.isDigit(currentChar)) {
                value.append(currentChar);
                advance();
            }
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

    private Token readDoubleQuotedString() {
        int startLine = line;
        int startCol = column;
        StringBuilder value = new StringBuilder();

        advance();

        while (currentChar != '\0' && currentChar != '"') {
            value.append(currentChar);
            advance();
        }

        if (currentChar == '"') {
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
           if (currentChar == ':' && peek() == ':') {
            advance();
            advance();
            return getNextToken();
        }

        int startLine = line;
        int startCol = column;

        if (Character.isLetter(currentChar) || currentChar == '_' || currentChar == '@') {
            return readIdentifierOrKeyword();
        }

        if (Character.isDigit(currentChar)) {
            return readNumber();
        }

        if (currentChar == '\'') {
            return readString();
        }

        if (currentChar == '"') {
            return readDoubleQuotedString();
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
            if (currentChar == '>') {
            advance();
            return new Token(TokenType.NOT_EQUAL, "<>", startLine, startCol);
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

        if (currentChar == '|') {
            advance();
            if (currentChar == '|') {
                advance();
                return new Token(TokenType.CONCAT, "||", startLine, startCol);
            }
            return new Token(TokenType.PIPE, "|", startLine, startCol);
        }

        if (currentChar == ':') {
            advance();
            return new Token(TokenType.COLON, ":", startLine, startCol);
        }

        if (currentChar == '$') {
            advance();
            return new Token(TokenType.DOLLAR, "$", startLine, startCol);
        }

        if (currentChar == '[') {
            advance();
            return new Token(TokenType.LBRACKET, "[", startLine, startCol);
        }

        if (currentChar == ']') {
            advance();
            return new Token(TokenType.RBRACKET, "]", startLine, startCol);
        }

        char ch = currentChar;
        advance();

      switch (ch) {
        case '=': return new Token(TokenType.EQUAL, "=", startLine, startCol);
        case '*': return new Token(TokenType.ASTERISK, "*", startLine, startCol);
        case ',': return new Token(TokenType.COMMA, ",", startLine, startCol);
        case ';': return new Token(TokenType.SEMICOLON, ";", startLine, startCol);
        case '(': return new Token(TokenType.LPAREN, "(", startLine, startCol);
        case ')': return new Token(TokenType.RPAREN, ")", startLine, startCol);
        case '.': return new Token(TokenType.DOT, ".", startLine, startCol);
        case '{': return new Token(TokenType.LBRACE, "{", startLine, startCol);
        case '}': return new Token(TokenType.RBRACE, "}", startLine, startCol);
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
