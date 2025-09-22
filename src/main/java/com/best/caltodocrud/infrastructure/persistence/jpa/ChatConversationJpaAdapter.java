package com.best.caltodocrud.infrastructure.persistence.jpa;

import com.best.caltodocrud.application.port.out.ChatConversationRepositoryPort;
import com.best.caltodocrud.domain.ChatConversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatConversation Repository Adapter
 * Domain과 Infrastructure 계층 간의 어댑터
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatConversationJpaAdapter implements ChatConversationRepositoryPort {
    
    private final SpringDataChatConversationRepository repository;
    private final ChatConversationMapper mapper;
    
    @Override
    @Transactional
    public ChatConversation save(ChatConversation conversation) {
        log.info("Saving chat conversation - sessionId: {}, messageCount: {}",
                conversation.getSessionId(),
                conversation.getMessages() != null ? conversation.getMessages().size() : 0);

        try {
            // mapper.toEntity()에서 이미 JSON 문자열로 변환됨
            ChatConversationEntity entity = mapper.toEntity(conversation);
            log.info("Entity created successfully with messages JSON length: {}",
                    entity.getMessages() != null ? entity.getMessages().length() : 0);

            ChatConversationEntity saved = repository.save(entity);
            log.info("Entity saved to database with ID: {}", saved.getId());

            return mapper.toDomain(saved);
        } catch (Exception e) {
            log.error("Failed to save chat conversation: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ChatConversation> findById(UUID id) {
        return repository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<ChatConversation> findActiveBySessionId(String sessionId) {
        return repository.findBySessionIdAndStatus(
            sessionId, 
            ChatConversation.ConversationStatus.ACTIVE
        ).map(mapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatConversation> findRecentConversations(int limit) {
        return repository.findByStatusOrderByLastMessageAtDesc(
            ChatConversation.ConversationStatus.ACTIVE
        ).stream()
            .limit(limit)
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatConversation> searchConversations(String keyword) {
        // JSON 검색 쿼리 생성
        String searchJson = String.format(
            "[{\"content\": \"*%s*\"}]", 
            keyword.replace("\"", "\\\"")
        );
        
        return repository.searchInMessages(searchJson).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatConversation> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByStartedAtBetween(start, end).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long calculateTokenUsage(LocalDateTime start, LocalDateTime end) {
        Long totalTokens = repository.calculateTotalTokensUsed(start, end);
        return totalTokens != null ? totalTokens : 0L;
    }
    
    @Override
    @Transactional
    public void archiveOldConversations(LocalDateTime before) {
        List<ChatConversationEntity> oldConversations = 
            repository.findByStartedAtBetween(LocalDateTime.MIN, before);
        
        oldConversations.forEach(entity -> {
            entity.setStatus(ChatConversation.ConversationStatus.ARCHIVED);
            repository.save(entity);
        });
        
        log.info("Archived {} old conversations", oldConversations.size());
    }
    
}
