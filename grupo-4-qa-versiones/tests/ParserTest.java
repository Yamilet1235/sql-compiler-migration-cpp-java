package com.sqlcompiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testSelectAllFromTable() {
        Lexer lexer = new Lexer("SELECT * FROM usuarios;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode result = parser.parse();

        assertNotNull(result);
        assertTrue(result.isSelectAll());
        assertEquals("usuarios", result.getTableName());
        assertNull(result.getWhereCondition());
    }

    @Test
    public void testSelectColumnsFromTable() {
        Lexer lexer = new Lexer("SELECT nombre, edad FROM usuarios;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode result = parser.parse();

        assertNotNull(result);
        assertFalse(result.isSelectAll());
        assertEquals(2, result.getColumns().size());
        assertEquals("nombre", result.getColumns().get(0));
        assertEquals("edad", result.getColumns().get(1));
        assertEquals("usuarios", result.getTableName());
    }

    @Test
    public void testSelectWithWhereEqual() {
        Lexer lexer = new Lexer("SELECT * FROM productos WHERE precio = 100;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode result = parser.parse();

        assertNotNull(result);
        assertNotNull(result.getWhereCondition());
        assertEquals(CompOperator.EQUAL, result.getWhereCondition().getOp());
        assertEquals(ExpressionNode.ExprType.IDENTIFIER, result.getWhereCondition().getLeft().getType());
        assertEquals("precio", result.getWhereCondition().getLeft().getValue());
        assertEquals(ExpressionNode.ExprType.NUMBER, result.getWhereCondition().getRight().getType());
        assertEquals("100", result.getWhereCondition().getRight().getValue());
    }

    @Test
    public void testSelectWithWhereString() {
        Lexer lexer = new Lexer("SELECT * FROM usuarios WHERE nombre = 'Juan'");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode result = parser.parse();

        assertNotNull(result);
        assertNotNull(result.getWhereCondition());
        assertEquals("nombre", result.getWhereCondition().getLeft().getValue());
        assertEquals(CompOperator.EQUAL, result.getWhereCondition().getOp());
        assertEquals("Juan", result.getWhereCondition().getRight().getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testMissingFrom() {
        Lexer lexer = new Lexer("SELECT * usuarios;");
        Parser parser = new Parser(lexer.tokenize());
        parser.parse();
    }

    @Test(expected = RuntimeException.class)
    public void testMissingSelect() {
        Lexer lexer = new Lexer("FROM usuarios;");
        Parser parser = new Parser(lexer.tokenize());
        parser.parse();
    }

    @Test
    public void testPrintAST() {
        Lexer lexer = new Lexer("SELECT nombre, edad FROM usuarios WHERE edad > 18;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode result = parser.parse();

        assertNotNull(result);
        System.out.println("--- AST Output ---");
        result.print(0);
        System.out.println("--- End AST ---");
    }
}
