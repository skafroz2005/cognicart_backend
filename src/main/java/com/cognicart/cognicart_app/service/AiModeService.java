package com.cognicart.cognicart_app.service;

import com.cognicart.cognicart_app.request.AiChatSendRequest;
import com.cognicart.cognicart_app.request.AiSessionCreateRequest;
import com.cognicart.cognicart_app.response.AiChatMessageResponse;
import com.cognicart.cognicart_app.response.AiChatSendResponse;
import com.cognicart.cognicart_app.response.AiChatSessionResponse;

import java.util.List;

public interface AiModeService {
    List<AiChatSessionResponse> getSessions(String jwt);
    AiChatSessionResponse createSession(String jwt, AiSessionCreateRequest request);
    List<AiChatMessageResponse> getMessages(String jwt, Long sessionId);
    AiChatSendResponse sendMessage(String jwt, AiChatSendRequest request);
}
