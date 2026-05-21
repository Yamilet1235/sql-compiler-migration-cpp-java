package com.sqlcompiler.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiServiceClient {

    @Value("${hf.api.token}")
    private String apiToken;

    private final String HF_URL = "https://router.huggingface.co/v1/chat/completions";

    private final RestTemplate restTemplate;

    public GeminiServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public String consultarGemini(String systemInstruction, String mensajeUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "meta-llama/Llama-3.2-1B-Instruct");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemInstruction));
        messages.add(Map.of("role", "user", "content", mensajeUsuario));
        requestBody.put("messages", messages);

        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(HF_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
            return "No se genero contenido.";
        } catch (Exception e) {
            return "Error al conectar con la IA: " + e.getMessage();
        }
    }
}
