package com.cognicart.cognicart_app.repository;

import com.cognicart.cognicart_app.model.AiChatSession;
import com.cognicart.cognicart_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    List<AiChatSession> findByUserOrderByUpdatedAtDesc(User user);
    Optional<AiChatSession> findByIdAndUser(Long id, User user);
}
