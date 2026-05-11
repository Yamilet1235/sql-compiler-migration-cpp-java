package com.sqlcompiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class LexerTest {

    @Test
    public void testSimpleSelect() {
        Lexer lexer = new Lexer("SELECT * FROM usuarios;");
        List<Token> tokens = lexer.tokenize();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.SELECT, tokens.get(0).getType());
        assertEquals(TokenType.ASTERISK, tokens.get(1).getType());
        assertEquals(TokenType.FROM, tokens.get(2).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals("usuarios", tokens.get(3).getValue());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).getType());
        assertEquals(TokenType.END_OF_FILE, tokens.get(5).getType());
    }

    @Test
    public void testSelectWithColumns() {
        Lexer lexer = new Lexer("SELECT nombre, edad FROM usuarios");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.SELECT, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("nombre", tokens.get(1).getValue());
        assertEquals(TokenType.COMMA, tokens.get(2).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals("edad", tokens.get(3).getValue());
        assertEquals(TokenType.FROM, tokens.get(4).getType());
    }

    @Test
    public void testSelectWithWhere() {
        Lexer lexer = new Lexer("SELECT * FROM productos WHERE precio > 100");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.SELECT, tokens.get(0).getType());
        assertEquals(TokenType.ASTERISK, tokens.get(1).getType());
        assertEquals(TokenType.FROM, tokens.get(2).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals("productos", tokens.get(3).getValue());
        assertEquals(TokenType.WHERE, tokens.get(4).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(5).getType());
        assertEquals("precio", tokens.get(5).getValue());
        assertEquals(TokenType.GREATER, tokens.get(6).getType());
        assertEquals(TokenType.NUMBER, tokens.get(7).getType());
        assertEquals("100", tokens.get(7).getValue());
    }

    @Test
    public void testStringLiteral() {
        Lexer lexer = new Lexer("SELECT * FROM usuarios WHERE nombre = 'Juan'");
        List<Token> tokens = lexer.tokenize();

        Token stringToken = tokens.get(7);
        assertEquals(TokenType.STRING, stringToken.getType());
        assertEquals("Juan", stringToken.getValue());
    }

    @Test
    public void testComment() {
        Lexer lexer = new Lexer("SELECT * -- esto es un comentario\nFROM tabla");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.SELECT, tokens.get(0).getType());
        assertEquals(TokenType.ASTERISK, tokens.get(1).getType());
        assertEquals(TokenType.FROM, tokens.get(2).getType());
    }

    @Test
    public void testComparisonOperators() {
        String[] ops = {"=", ">", "<", ">=", "<=", "!="};
        TokenType[] expected = {
            TokenType.EQUAL, TokenType.GREATER, TokenType.LESS,
            TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL, TokenType.NOT_EQUAL
        };

        for (int i = 0; i < ops.length; i++) {
            Lexer lexer = new Lexer("SELECT * FROM t WHERE a " + ops[i] + " 1");
            List<Token> tokens = lexer.tokenize();
            assertEquals("Operator: " + ops[i], expected[i], tokens.get(6).getType());
        }
    }

    @Test
    public void testEmptyInput() {
        Lexer lexer = new Lexer("");
        List<Token> tokens = lexer.tokenize();

        assertEquals(1, tokens.size());
        assertEquals(TokenType.END_OF_FILE, tokens.get(0).getType());
    }

    @Test
    public void testInvalidToken() {
        Lexer lexer = new Lexer("SELECT @ FROM t");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.INVALID, tokens.get(1).getType());
    }
}
