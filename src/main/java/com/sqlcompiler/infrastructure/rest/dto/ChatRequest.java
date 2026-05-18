package com.sqlcompiler.infrastructure.rest.dto;

public class ChatRequest {
    private String prompt;

    // Constructor vacío requerido por Jackson para deserializar JSON
    public ChatRequest() {}

    public ChatRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}