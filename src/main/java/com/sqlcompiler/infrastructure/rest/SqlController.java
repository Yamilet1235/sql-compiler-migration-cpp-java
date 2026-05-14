package com.sqlcompiler.infrastructure.rest;

import com.sqlcompiler.domain.lexer.Lexer;
import com.sqlcompiler.domain.lexer.Token;
import com.sqlcompiler.domain.parser.*;
import com.sqlcompiler.domain.semantic.DialectDetector;
import com.sqlcompiler.domain.semantic.SemanticAnalyzer;
import com.sqlcompiler.domain.semantic.SymbolTable;
import com.sqlcompiler.infrastructure.rest.dto.ValidationRequest;
import com.sqlcompiler.infrastructure.rest.dto.ValidationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/validate")
@CrossOrigin(origins = "*")
public class SqlController {

    @PostMapping("/query")
    public ResponseEntity<ValidationResponse> validateQuery(@RequestBody ValidationRequest request) {
        ValidationResponse response = new ValidationResponse();
        try {
            String query = request.getQuery();
            String dialect = request.getDialect();
            if (dialect == null || dialect.isEmpty()) {
                dialect = DialectDetector.detect(query);
            }

            // 1. ANALISIS LEXICO
            Lexer lexer = new Lexer(query, dialect);
            List<Token> tokens = lexer.tokenize();

            List<ValidationResponse.TokenInfo> tokenInfos = new ArrayList<>();
            for (Token t : tokens) {
                if (t.getType() == com.sqlcompiler.domain.lexer.TokenType.END_OF_FILE) continue;
                ValidationResponse.TokenInfo ti = new ValidationResponse.TokenInfo();
                ti.setType(t.typeToString());
                ti.setValue(t.getValue());
                ti.setLine(t.getLine());
                ti.setColumn(t.getColumn());
                tokenInfos.add(ti);
            }
            response.setTokens(tokenInfos);

            // 2. ANALISIS SINTACTICO
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            // Capturar el AST como string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);
            ast.print(0);
            System.out.flush();
            System.setOut(old);
            response.setAst(baos.toString());

            // 3. ANALISIS SEMANTICO
            SemanticAnalyzer analyzer = new SemanticAnalyzer(new SymbolTable());
            boolean valid = analyzer.analyze(ast);
            response.setValid(valid);
            response.setErrors(analyzer.getErrors());
            response.setWarnings(analyzer.getWarnings());

        } catch (RuntimeException e) {
            response.setValid(false);
            if (response.getErrors() == null) {
                response.setErrors(new ArrayList<>());
            }
            response.getErrors().add("Error: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}