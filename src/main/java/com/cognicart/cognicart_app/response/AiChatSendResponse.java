package com.cognicart.cognicart_app.response;

import com.cognicart.cognicart_app.model.Product;

import java.util.ArrayList;
import java.util.List;

public class AiChatSendResponse {
    private Long sessionId;
    private AiChatMessageResponse userMessage;
    private AiChatMessageResponse assistantMessage;
    private List<Product> products = new ArrayList<>();
    private AiSuggestedActionResponse action;
    private boolean usedAi;
    private String provider;
    private String model;
    private String aiError;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public AiChatMessageResponse getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(AiChatMessageResponse userMessage) {
        this.userMessage = userMessage;
    }

    public AiChatMessageResponse getAssistantMessage() {
        return assistantMessage;
    }

    public void setAssistantMessage(AiChatMessageResponse assistantMessage) {
        this.assistantMessage = assistantMessage;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public AiSuggestedActionResponse getAction() {
        return action;
    }

    public void setAction(AiSuggestedActionResponse action) {
        this.action = action;
    }

    public boolean isUsedAi() {
        return usedAi;
    }

    public void setUsedAi(boolean usedAi) {
        this.usedAi = usedAi;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getAiError() {
        return aiError;
    }

    public void setAiError(String aiError) {
        this.aiError = aiError;
    }
}
