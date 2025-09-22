package com.best.caltodocrud.infrastructure.persistence.jpa;

import com.best.caltodocrud.domain.ChatConversation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 챗봇 대화 JPA 엔티티
 * PostgreSQL의 JSONB 타입을 활용한 대화 내역 저장
 */
@Entity
@Table(name = "chat_conversations", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_started_at", columnList = "started_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatConversation.ConversationStatus status;
    
    /**
     * 메시지 배열을 JSONB로 저장
     */
    @Column(columnDefinition = "jsonb", nullable = false)
    private String messages;

    /**
     * 대화 메타데이터를 JSONB로 저장
     */
    @Column(columnDefinition = "jsonb")
    private String metadata;

    /**
     * AI 모델 응답 전체를 JSONB로 저장
     */
    @Column(name = "ai_responses", columnDefinition = "jsonb")
    private String aiResponses;

    /**
     * 사용자 컨텍스트 정보를 JSONB로 저장
     */
    @Column(name = "user_context", columnDefinition = "jsonb")
    private String userContext;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
