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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/validate")
@CrossOrigin(origins = "*")
public class SqlController {

   
    private static String inMemoryJsonSchema = null;

    @PostMapping("/schema/upload")
    public ResponseEntity<?> uploadSchema(@RequestBody String sqlContent) {
        try {
            StringBuilder json = new StringBuilder("{\"tablas\": [");
            
           
            Pattern tablePattern = Pattern.compile("(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\w+)\\s*\\((.*?)\\);", Pattern.DOTALL);
            Matcher tableMatcher = tablePattern.matcher(sqlContent);
            boolean firstTable = true;

            while (tableMatcher.find()) {
                if (!firstTable) json.append(",");
                String tableName = tableMatcher.group(1);
                String columnsBlock = tableMatcher.group(2);
                
                json.append("{\"nombre\": \"").append(tableName).append("\", \"columnas\": [");
                
                String[] lines = columnsBlock.split(",");
                boolean firstCol = true;
                
                for (String line : lines) {
                    line = line.trim();
                    
                    if (line.isEmpty() || line.toUpperCase().startsWith("CONSTRAINT") || line.toUpperCase().startsWith("PRIMARY") || line.toUpperCase().startsWith("FOREIGN")) {
                        continue;
                    }
                    String colName = line.split("\\s+")[0];
                    if (!firstCol) json.append(",");
                    json.append("\"").append(colName).append("\"");
                    firstCol = false;
                }
                json.append("]}");
                firstTable = false;
            }
            json.append("]}");
            
          
            inMemoryJsonSchema = json.toString();
            
            return ResponseEntity.ok("{\"mensaje\": \"Base de datos procesada correctamente\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Error al procesar el archivo SQL\"}");
        }
    }

    @PostMapping("/query")
    public ResponseEntity<ValidationResponse> validateQuery(@RequestBody ValidationRequest request) {
        ValidationResponse response = new ValidationResponse();
        try {
            String query = request.getQuery();
            String dialect = request.getDialect();
            
            if (query == null || query.trim().isEmpty()) {
                response.setValid(false);
                response.setErrors(List.of("La consulta esta vacia"));
                return ResponseEntity.ok(response);
            }
            if (dialect == null || dialect.isEmpty()) {
                dialect = DialectDetector.detect(query);
            }
            
            
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

           
            Parser parser = new Parser(tokens, dialect);
            ASTNode ast = parser.parse();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);
            ast.print(0);
            System.out.flush();
            System.setOut(old);
            response.setAst(baos.toString());
            
            response.setAst(baos.toString()); 

            
            if (ast != null) {
                response.setAstData(ast.toVisualTree());
            }
            
            
            SymbolTable symTable = new SymbolTable(null); 
            
            if (inMemoryJsonSchema != null) {
                symTable.loadSchema(inMemoryJsonSchema);
            }

            SemanticAnalyzer analyzer = new SemanticAnalyzer(symTable);
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