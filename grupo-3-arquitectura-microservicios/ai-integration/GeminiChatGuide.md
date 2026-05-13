# Integración con Gemini API

## Opción 1: REST directo (recomendado, sin SDK)

```java
import java.net.http.*;
import java.net.URI;

public class GeminiClient {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String ask(String prompt) throws Exception {
        String json = """
            {"contents":[{"parts":[{"text":"%s"}]}]}
            """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
```

## Opción 2: Con Spring Cloud OpenFeign

Agregar dependencia:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## Prompt sugerido para el chat de ayuda

```
Eres un asistente experto en SQL.
El usuario escribió: "{consulta}"
El error fue: "{error}"
Dialecto detectado: {dialecto}

Explica el error en español y sugiere una corrección.
Si no hay error, explica qué hace la consulta.
```

## Uso: desde el frontend
El frontend llama a:
- `POST /api/v1/chat` con `{ "query": "...", "error": "...", "dialect": "..." }`
- El backend reenvía a Gemini y retorna la respuesta al frontend
