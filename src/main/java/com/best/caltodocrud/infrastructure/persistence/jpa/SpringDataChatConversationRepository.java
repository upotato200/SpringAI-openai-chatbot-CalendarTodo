package com.best.caltodocrud.infrastructure.persistence.jpa;

import com.best.caltodocrud.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ChatConversation
 * PostgreSQL의 JSON 함수를 활용한 쿼리 지원
 */
@Repository
public interface SpringDataChatConversationRepository extends JpaRepository<ChatConversationEntity, UUID> {
    
    // 세션 ID로 조회
    Optional<ChatConversationEntity> findBySessionIdAndStatus(
        String sessionId, 
        ChatConversation.ConversationStatus status
    );
    
    // 최근 활성 대화 조회
    List<ChatConversationEntity> findByStatusOrderByLastMessageAtDesc(
        ChatConversation.ConversationStatus status
    );
    
    // 기간별 대화 조회
    List<ChatConversationEntity> findByStartedAtBetween(
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * PostgreSQL의 JSONB 연산자를 사용한 검색
     * messages 배열 내에서 특정 키워드 검색
     */
    @Query(value = """
        SELECT c.* FROM chat_conversations c
        WHERE c.messages @> :searchJson::jsonb
        ORDER BY c.last_message_at DESC
        """, nativeQuery = true)
    List<ChatConversationEntity> searchInMessages(@Param("searchJson") String searchJson);

    /**
     * 특정 주제가 논의된 대화 검색
     * metadata의 topicsDiscussed 배열에서 검색
     */
    @Query(value = """
        SELECT c.* FROM chat_conversations c
        WHERE jsonb_exists(c.metadata->'topicsDiscussed', :topic)
        ORDER BY c.last_message_at DESC
        """, nativeQuery = true)
    List<ChatConversationEntity> findByTopic(@Param("topic") String topic);

    /**
     * 토큰 사용량 통계
     */
    @Query(value = """
        SELECT COALESCE(SUM(CAST(c.metadata->>'totalTokensUsed' AS INTEGER)), 0)
        FROM chat_conversations c
        WHERE c.started_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    Long calculateTotalTokensUsed(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 요약 타입별 대화 검색
     */
    @Query(value = """
        SELECT c.* FROM chat_conversations c, 
        jsonb_array_elements(c.messages) msg
        WHERE msg->'metadata'->>'summaryType' = :summaryType
        ORDER BY c.last_message_at DESC
        """, nativeQuery = true)
    List<ChatConversationEntity> findBySummaryType(@Param("summaryType") String summaryType);
}
