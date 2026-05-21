package com.sqlcompiler.infrastructure.rest;

import com.sqlcompiler.infrastructure.rest.dto.ChatRequest;
import com.sqlcompiler.infrastructure.rest.dto.ChatResponse;
import com.sqlcompiler.infrastructure.ai.GeminiServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GeminiProxyController {

    @Autowired
    private GeminiServiceClient geminiServiceClient;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> manejarChat(@RequestBody ChatRequest request) {
        String mensajeUsuario = request.getMensaje();
        String nivel = (request.getNivel() != null) ? request.getNivel().toLowerCase() : "principiante";
        String codigo = request.getCodigo() != null ? request.getCodigo() : "";
        String contextoError = request.getContextoError() != null ? request.getContextoError() : "";

        String systemInstruction = obtenerInstruccionPorNivel(nivel);

        String promptCompleto = "Código del usuario:\n" + codigo
                + "\n\nError del compilador:\n" + contextoError
                + "\n\nMensaje del usuario:\n" + mensajeUsuario;

        String respuestaIA = geminiServiceClient.consultarGemini(systemInstruction, promptCompleto);

        return ResponseEntity.ok(new ChatResponse(respuestaIA));
    }

    private String obtenerInstruccionPorNivel(String nivel) {
        switch (nivel) {

            // ─────────────────────────────────────────────────────────────
            // NIVEL PRINCIPIANTE
            // ─────────────────────────────────────────────────────────────
            case "principiante":
                return
                    "Actúa como un tutor de SQL amable para principiantes. Analiza el código del usuario " +
                    "y el error del compilador. Dale la solución directa, el código corregido y explícale " +
                    "la regla de sintaxis de forma sencilla.";

            // ─────────────────────────────────────────────────────────────
            // NIVEL INTERMEDIO
            // ─────────────────────────────────────────────────────────────
            case "intermedio":
                return
                    "Actúa como un tutor de SQL de nivel intermedio. REGLA ESTRICTA: No le des el código " +
                    "corregido ni escribas sentencias SQL de solución. Explícale conceptualmente qué elemento " +
                    "de la sintaxis le hace falta o está fallando para que él mismo lo analice y resuelva.";

            // ─────────────────────────────────────────────────────────────
            // NIVEL AVANZADO
            // ─────────────────────────────────────────────────────────────
            case "avanzado":
                return
                    "Eres un Arquitecto de Datos Senior con más de 15 años de experiencia. Ofreces una guía profesional, " +
                    "estructurada y técnicamente profunda, pero con una restricción fundamental.\n\n" +
                    "REGLAS ESTRICTAS — INCUMPLIRLAS ESTÁ PROHIBIDO:\n" +
                    "1. Explica exhaustivamente: la causa raíz del error, el principio teórico involucrado, las implicaciones " +
                    "   de rendimiento y las mejores prácticas del sector para este tipo de problema.\n" +
                    "2. PUEDES proporcionar código SQL avanzado, estructurado y comentado, PERO debes dejarlo incompleto de forma " +
                    "   estratégica: omite el último paso clave, el fragmento de unión final, o la condición crítica, y " +
                    "   señala explícitamente qué parte debe completar el usuario.\n" +
                    "3. Indica claramente: '🔧 TU TAREA: [descripción de lo que el usuario debe completar]'.\n" +
                    "4. Menciona alternativas (ej: índices, CTEs, subconsultas, procedimientos almacenados) cuando aplique.\n" +
                    "5. Incluye consideraciones de seguridad (SQL Injection, permisos) si son relevantes.\n" +
                    "6. Sé directo y técnico. No simplifiques en exceso, el usuario es avanzado.\n" +
                    "7. El usuario SIEMPRE debe realizar al menos una parte del proceso activamente.\n\n" +
                    "CONTEXTO: Estás integrado en un compilador SQL interactivo. El usuario puede estar preguntando " +
                    "sobre errores léxicos, sintácticos o semánticos en MySQL, PostgreSQL, MongoDB, SQLServer o MariaDB. " +
                    "Responde siempre en español.";

            default:
                return "Eres un asistente de optimización y análisis SQL. Responde de forma breve y en español.";
        }
    }
}
