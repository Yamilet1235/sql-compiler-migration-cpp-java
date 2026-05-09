package com.sqlcompiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("  Compilador SQL - Mini Lenguaje");
        System.out.println("  Curso de Compiladores 2026 - UMG");
        System.out.println("  Autor: Richard Ortiz");
        System.out.println("============================================");
        System.out.println();

        String query;

        if (args.length >= 1) {
            String filename = args[0];
            System.out.println("[ARCHIVO] " + filename);
            try {
                query = readFile(filename);
            } catch (IOException e) {
                System.err.println("Error: No se pudo abrir el archivo '" + filename + "'");
                return;
            }
        } else {
            System.out.println("[MODO INTERACTIVO] Escriba su query SQL:");
            System.out.print("> ");
            Scanner scanner = new Scanner(System.in);
            query = scanner.nextLine();
        }

        System.out.println();
        System.out.println("--- QUERY DE ENTRADA ---");
        System.out.println(query);
        System.out.println("------------------------");
        System.out.println();

        System.out.println("========== FASE 1: ANALISIS LEXICO ==========");

        Lexer lexer = new Lexer(query);
        List<Token> tokens = lexer.tokenize();

        System.out.println("Tokens encontrados: " + tokens.size());
        System.out.println();

        for (Token token : tokens) {
            System.out.println("  " + token);
        }
        System.out.println();

        System.out.println("========== FASE 2: ANALISIS SINTACTICO ==========");

        try {
            Parser parser = new Parser(tokens);
            SelectNode ast = parser.parse();

            System.out.println("Parsing exitoso. Arbol de Sintaxis Abstracta (AST):");
            System.out.println();
            ast.print(2);
            System.out.println();

            System.out.println("========== FASE 3: ANALISIS SEMANTICO ==========");

            SymbolTable symbolTable = new SymbolTable();

            System.out.println("Schema de referencia:");
            symbolTable.print();
            System.out.println();

            SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
            boolean valid = analyzer.analyze(ast);

            analyzer.printDiagnostics();
            System.out.println();

            System.out.println("========== RESULTADO ==========");
            if (valid) {
                System.out.println("La query SQL es VALIDA sintactica y semanticamente.");
            } else {
                System.out.println("La query SQL tiene ERRORES.");
            }
            System.out.println();

        } catch (RuntimeException e) {
            System.out.println();
            System.out.println("ERROR DE SINTAXIS:");
            System.out.println("  " + e.getMessage());
            System.out.println();
            System.out.println("========== RESULTADO ==========");
            System.out.println("La query SQL tiene ERRORES de sintaxis.");
            System.out.println();
        }
    }
}
