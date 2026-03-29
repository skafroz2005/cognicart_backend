package com.cognicart.cognicart_app.service;

import com.cognicart.cognicart_app.request.AttributeExtractionRequest;
import com.cognicart.cognicart_app.response.AiHealthCheckResponse;
import com.cognicart.cognicart_app.response.AttributeExtractionResponse;

public interface AttributeExtractionService {
    AttributeExtractionResponse extractAttributes(AttributeExtractionRequest request);
    AiHealthCheckResponse checkHealth();
}
