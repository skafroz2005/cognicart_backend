package com.cognicart.cognicart_app.repository;

import com.cognicart.cognicart_app.model.AiChatMessage;
import com.cognicart.cognicart_app.model.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
    List<AiChatMessage> findBySessionOrderByCreatedAtAsc(AiChatSession session);
    List<AiChatMessage> findTop10BySessionOrderByCreatedAtDesc(AiChatSession session);
}
