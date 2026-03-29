package com.cognicart.cognicart_app.response;

public class AiHealthCheckResponse {

    private boolean configured;
    private String provider;
    private String status;
    private String activeModel;
    private String message;

    public AiHealthCheckResponse() {
    }

    public AiHealthCheckResponse(boolean configured, String provider, String status, String activeModel, String message) {
        this.configured = configured;
        this.provider = provider;
        this.status = status;
        this.activeModel = activeModel;
        this.message = message;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActiveModel() {
        return activeModel;
    }

    public void setActiveModel(String activeModel) {
        this.activeModel = activeModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
