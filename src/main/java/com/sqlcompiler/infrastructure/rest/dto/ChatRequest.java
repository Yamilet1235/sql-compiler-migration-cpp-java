package com.sqlcompiler.infrastructure.rest.dto;

public class ChatRequest {
    private String mensaje;
    private String nivel; 
    private String contextoError; 

    public ChatRequest() {}

    public ChatRequest(String mensaje, String nivel) {
        this.mensaje = mensaje;
        this.nivel = nivel;
    }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
}