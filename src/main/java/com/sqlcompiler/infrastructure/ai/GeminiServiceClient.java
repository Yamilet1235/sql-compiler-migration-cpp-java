package com.sqlcompiler.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiServiceClient {

    @Value("${AIzaSyCcXBdPSYFXZ3iayPaW6T2VHuDn_kyFnnQ}")
    private String apiKey;

    // ✅ FIX: Cambiado de /v1/ a /v1beta/ para soportar el campo "system_instruction"
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private final RestTemplate restTemplate;

    public GeminiServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public String consultarGemini(String systemInstruction, String mensajeUsuario) {
        String urlCompleta = GEMINI_URL + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();

        // 1. System Instruction — funciona correctamente en v1beta
        Map<String, Object> systemInstructionMap = new HashMap<>();
        Map<String, Object> partsSystem = new HashMap<>();
        partsSystem.put("text", systemInstruction);
        systemInstructionMap.put("parts", Collections.singletonList(partsSystem));
        requestBody.put("system_instruction", systemInstructionMap);

        // 2. Contenido del usuario
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("role", "user");
        Map<String, Object> partsUser = new HashMap<>();
        partsUser.put("text", mensajeUsuario);
        contentMap.put("parts", Collections.singletonList(partsUser));
        requestBody.put("contents", Collections.singletonList(contentMap));

        // 3. Configuración de generación (opcional pero recomendado)
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(urlCompleta, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extraerTextoDeRespuesta(response.getBody());
            }
            return "⚠️ Error: No se recibió una respuesta válida de Gemini.";
        } catch (Exception e) {
            return "❌ Error al conectar con la API de Gemini: " + e.getMessage();
        }
    }

    private String extraerTextoDeRespuesta(Map responseBody) {
        try {
            List candidates = (List) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map firstCandidate = (Map) candidates.get(0);
                Map content = (Map) firstCandidate.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map firstPart = (Map) parts.get(0);
                        return (String) firstPart.get("text");
                    }
                }
            }
        } catch (Exception e) {
            return "⚠️ Error interpretando la respuesta del modelo de IA.";
        }
        return "No se generó contenido.";
    }
}
