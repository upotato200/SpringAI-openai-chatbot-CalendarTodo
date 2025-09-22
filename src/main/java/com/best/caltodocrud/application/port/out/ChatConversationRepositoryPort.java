package com.best.caltodocrud.application.port.out;

import com.best.caltodocrud.domain.ChatConversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ChatConversation Repository Port
 * 아웃바운드 포트 - 대화 저장소 인터페이스
 */
public interface ChatConversationRepositoryPort {
    
    ChatConversation save(ChatConversation conversation);
    
    Optional<ChatConversation> findById(UUID id);
    
    Optional<ChatConversation> findActiveBySessionId(String sessionId);
    
    List<ChatConversation> findRecentConversations(int limit);
    
    List<ChatConversation> searchConversations(String keyword);
    
    List<ChatConversation> findByDateRange(LocalDateTime start, LocalDateTime end);
    
    Long calculateTokenUsage(LocalDateTime start, LocalDateTime end);
    
    void archiveOldConversations(LocalDateTime before);
}
