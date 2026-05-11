package com.sqlcompiler.infrastructure.rest;

import com.sqlcompiler.infrastructure.rest.dto.ValidationRequest;
import com.sqlcompiler.infrastructure.rest.dto.ValidationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validate")
@CrossOrigin(origins = "*")
public class SqlController {

    @PostMapping("/query")
    public ResponseEntity<ValidationResponse> validateQuery(@RequestBody ValidationRequest request) {
        ValidationResponse response = new ValidationResponse();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schema")
    public ResponseEntity<String> loadSchema(@RequestBody String schemaJson) {
        return ResponseEntity.ok("Schema cargado exitosamente");
    }

    @GetMapping("/dialects")
    public ResponseEntity<String[]> getSupportedDialects() {
        String[] dialects = {"mysql", "postgresql", "mariadb", "sqlite"};
        return ResponseEntity.ok(dialects);
    }
}
