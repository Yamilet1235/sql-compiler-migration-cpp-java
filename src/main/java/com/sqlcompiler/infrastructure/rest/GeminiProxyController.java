package com.sqlcompiler.infrastructure.rest;

import com.sqlcompiler.infrastructure.rest.dto.ChatRequest;
import com.sqlcompiler.infrastructure.rest.dto.ChatResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "http://localhost:5173") 
public class GeminiProxyController {

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> manejarChat(@RequestBody ChatRequest request) {
        // Tu lógica para llamar a Gemini usando request.getPrompt()...
        
        // Ejemplo de retorno (aquí irá la respuesta real de tu servicio de IA)
        String textoIA = "Análisis completado desde Spring Boot."; 
        
        return ResponseEntity.ok(new ChatResponse(textoIA));
    }
}