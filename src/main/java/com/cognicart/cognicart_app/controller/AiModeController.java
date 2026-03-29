package com.cognicart.cognicart_app.controller;

import com.cognicart.cognicart_app.request.AiChatSendRequest;
import com.cognicart.cognicart_app.request.AiSessionCreateRequest;
import com.cognicart.cognicart_app.response.AiChatMessageResponse;
import com.cognicart.cognicart_app.response.AiChatSendResponse;
import com.cognicart.cognicart_app.response.AiChatSessionResponse;
import com.cognicart.cognicart_app.service.AiModeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai-mode")
public class AiModeController {

    private final AiModeService aiModeService;

    public AiModeController(AiModeService aiModeService) {
        this.aiModeService = aiModeService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AiChatSessionResponse>> getSessions(@RequestHeader("Authorization") String jwt) {
        return new ResponseEntity<>(aiModeService.getSessions(jwt), HttpStatus.OK);
    }

    @PostMapping("/sessions")
    public ResponseEntity<AiChatSessionResponse> createSession(
            @RequestHeader("Authorization") String jwt,
            @RequestBody(required = false) AiSessionCreateRequest request
    ) {
        return new ResponseEntity<>(aiModeService.createSession(jwt, request), HttpStatus.CREATED);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<AiChatMessageResponse>> getSessionMessages(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long sessionId
    ) {
        return new ResponseEntity<>(aiModeService.getMessages(jwt, sessionId), HttpStatus.OK);
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatSendResponse> chat(
            @RequestHeader("Authorization") String jwt,
            @RequestBody AiChatSendRequest request
    ) {
        return new ResponseEntity<>(aiModeService.sendMessage(jwt, request), HttpStatus.OK);
    }
}
