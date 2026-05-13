package com.sqlcompiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class SemanticAnalyzerTest {

    @Test
    public void testValidQuery() {
        Lexer lexer = new Lexer("SELECT * FROM usuarios;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode ast = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
        boolean valid = analyzer.analyze(ast);

        assertTrue(valid);
        assertTrue(analyzer.getErrors().isEmpty());
    }

    @Test
    public void testTableNotFound() {
        Lexer lexer = new Lexer("SELECT nombre FROM clientes;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode ast = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
        boolean valid = analyzer.analyze(ast);

        assertFalse(valid);
        assertFalse(analyzer.getErrors().isEmpty());
        assertTrue(analyzer.getErrors().get(0).contains("clientes"));
    }

    @Test
    public void testColumnNotFound() {
        Lexer lexer = new Lexer("SELECT email FROM usuarios;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode ast = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
        boolean valid = analyzer.analyze(ast);

        assertFalse(valid);
        assertTrue(analyzer.getErrors().get(0).contains("email"));
    }

    @Test
    public void testTypeMismatch() {
        Lexer lexer = new Lexer("SELECT nombre FROM usuarios WHERE edad > 'hola';");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode ast = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
        boolean valid = analyzer.analyze(ast);

        assertFalse(valid);
        assertTrue(analyzer.getErrors().get(0).contains("incompatibles"));
    }

    @Test
    public void testValidQueryWithWhere() {
        Lexer lexer = new Lexer("SELECT nombre, edad FROM usuarios WHERE edad > 18;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode ast = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
        boolean valid = analyzer.analyze(ast);

        assertTrue(valid);
    }

    @Test
    public void testFloatIntCompatibility() {
        Lexer lexer = new Lexer("SELECT * FROM productos WHERE precio >= 100;");
        Parser parser = new Parser(lexer.tokenize());
        SelectNode ast = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
        boolean valid = analyzer.analyze(ast);

        assertTrue(valid);
    }
}
